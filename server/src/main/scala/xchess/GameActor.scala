package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCode
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import xchess.GameActor._
import xchess.logic.{Board, Pawn, Plan, XY}

import java.lang.System.{currentTimeMillis => now}
import scala.util.chaining.scalaUtilChainingOps

object GameActor {
  def apply(name: String, gameType: String, initialFreeze: Long, freeze: Long): Either[StatusCode, Props] =
    Right(Props(new GameActor(name, GameState(Board(gameType)), now + initialFreeze * 1000, freeze * 1000)))

  case object ClientConnected
  case object ClientDisconnected
  case class FromClient(message: String)

  private case class GameState(board: Board, clients: Set[ActorRef] = Set(), winner: Option[String] = None)
}
class GameActor(name: String, initialState: GameState, initialFreezeUntil: Long, freeze: Long) extends Actor with ActorLogging {
  override def preStart(): Unit = log.info(s"'$name' Started")
  override def postStop(): Unit = log.debug(s"'$name' Stopped")
  override def receive: Receive = game(initialState)
  def game(state: GameState): Receive = {
    case ClientConnected =>
      context.become(game(state.copy(clients = state.clients + sender())))
      send(state.board.size)
      state.board.map.foreach { case xy -> piece =>
        send(Add(piece.id, xy.x, xy.y, piece.color, piece.name, freeze(piece.since)))
        piece.plan.foreach { plan => send(ClientPlan(plan.pid, piece.color, xy, plan.pxy)) }
      }
      state.winner.foreach(winner => send(Winner(winner)))
    case ClientDisconnected =>
      context.become(game(state.copy(clients = state.clients - sender())))
    case As(ClientMove(id, x, y)) =>
      val to = XY(x,y)
      import state.board
      import board.map
      map.find { case (_, piece) => piece.id == id } foreach { case (xy, gamePiece) =>
        if (frozen(gamePiece.since)) {
          // Plam move
          gamePiece.plan.foreach { plan => send(Unplan(plan.pid, moved = false)) }
          val newPlan =
            if (!gamePiece.moves(xy)(board.size).flatten.contains(to)) None
            else Plan(to).tap(p => send(ClientPlan(p.pid, gamePiece.color, xy, p.pxy))).pipe(Some(_))
          val newPiece = gamePiece.copy(plan = newPlan)
          context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
        } else {
          // Execute move
          // There is not supposed to be any plan because it's not frozen, yet let's make sure...
          gamePiece.plan.foreach { plan => send(Unplan(plan.pid, moved = true)) }
          gamePiece.moves(xy)(board.size).find(_.contains(to)) match {
            case None =>
              log.info(s"Illegal move to $to for $gamePiece")
              // Illegal move for this piece
              val newPiece = gamePiece.copy(plan = None, since = now)
              context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
            case Some(steps) =>
              val intermediate = steps.takeWhile(_ != to)
              if (intermediate.exists(map.contains)) {
                log.info(s"Move illegal because a piece is in the way to $to for $gamePiece")
                // Move illegal because a piece is in the way
                val newPiece = gamePiece.copy(plan = None, since = now)
                context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
              } else {
                if (gamePiece.piece == Pawn && xy.x != to.x && !map.contains(to)) {
                  log.info(s"Pawn may not move diagonally unless there's a piece there to $to for $gamePiece")
                  // Pawn may not move diagonally unless there's a piece there
                  val newPiece = gamePiece.copy(plan = None, since = now)
                  context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
                } else {
                  log.info(s"$xy -> $to, $gamePiece, ${map.get(to)}")
                  if (gamePiece.piece == Pawn && xy.x == to.x && map.contains(to)) {
                    log.info(s"Pawn is blocked from moving forward to $to for $gamePiece")
                    // Pawn is blocked from moving forward
                    val newPiece = gamePiece.copy(plan = None, since = now)
                    context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
                  } else {
                    if (map.get(to).exists(_.color == gamePiece.color)) {
                      log.info(s"Can't capture own piece to $to for $gamePiece")
                      // Can't capture own piece
                      val newPiece = gamePiece.copy(plan = None, since = now)
                      context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
                    } else {
                      val newPiece = gamePiece.copy(plan = None, since = now)
                      val newMap = map - xy + (XY(x,y) -> newPiece)
                      send(Move(newPiece.id, x, y, freeze(newPiece.since)))
                      context.become(game(state.copy(board = board.copy(map = newMap))))
                    }
                  }
                }
              }
          }
        }
      }
    case m =>
      log.info(s"received $m")
  }
  def send[T: Encoder](msg: T): Unit = sender() ! WSHandler.ForWS(msg.asJson.noSpaces)
  def freeze(since: Long): Long = math.max(math.max(0, initialFreezeUntil - now), since + freeze - now)
  def frozen(since: Long): Boolean = now < initialFreezeUntil || now < since + freeze
}
