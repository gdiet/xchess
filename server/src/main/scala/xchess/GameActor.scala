package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCode
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import xchess.GameActor._
import xchess.logic.{Board, King, Pawn, Plan, Queen, XY}

import java.lang.System.{currentTimeMillis => now}
import java.util.concurrent.TimeUnit
import scala.concurrent.duration.Duration
import scala.util.chaining.scalaUtilChainingOps

object GameActor {
  def apply(name: String, gameType: String, initialFreeze: Long, freeze: Long): Either[StatusCode, Props] =
    Right(Props(new GameActor(name, GameState(Board(gameType)), now + initialFreeze * 1000, freeze * 1000)))

  case object ClientConnected
  case object ClientDisconnected
  case object TerminateIfUnconnected
  case class ExecutePlan(id: Int)
  case class FromClient(message: String)

  private case class GameState(board: Board, clients: Set[ActorRef] = Set(), winner: Option[String] = None)
}
class GameActor(name: String, initialState: GameState, initialFreezeUntil: Long, freeze: Long) extends Actor with ActorLogging {
  override def preStart(): Unit = log.info(s"Started game '$name'")
  override def postStop(): Unit = log.debug(s"Stopped game '$name'")
  override def receive: Receive = game(initialState)
  def scheduleTerminationCheck(): Unit = {
    log.debug("Schedule termination check")
    import context.dispatcher
    context.system.scheduler.scheduleOnce(Duration(10, TimeUnit.SECONDS), self, TerminateIfUnconnected)
  }
  scheduleTerminationCheck()
  def game(state: GameState): Receive = {
    case ClientConnected =>
      def reply[T: Encoder](msg: T): Unit = send(sender())(msg)
      context.become(game(state.copy(clients = state.clients + sender())))
      reply(state.board.size)
      state.board.map.foreach { case xy -> piece =>
        reply(Add(piece.id, xy.x, xy.y, piece.color, piece.name, freeze(piece.since)))
        piece.plan.foreach { plan => reply(ClientPlan(plan.pid, piece.color, xy, plan.pxy)) }
      }
      state.winner.foreach(winner => reply(Winner(winner)))
    case ClientDisconnected =>
      val clients = state.clients - sender()
      log.debug(s"Client disconnected, ${clients.size} clients connected")
      context.become(game(state.copy(clients = clients)))
      if (clients.isEmpty) scheduleTerminationCheck()
    case TerminateIfUnconnected =>
      log.debug(s"Termination check: ${state.clients.size} clients connected")
      if (state.clients.isEmpty) context.stop(self)
    case ExecutePlan(id) =>
      state.board.map.find { case (_, piece) => piece.id == id } foreach { case (xy, gamePiece) =>
        gamePiece.plan.foreach(plan => self ! ClientMove(id, plan.pxy.x, plan.pxy.y))
      }
    case As(clientMove) =>
      self ! clientMove
    case ClientMove(id, x, y) =>
      def broadcast[T: Encoder](msg: T): Unit = state.clients.foreach(send(_)(msg))
      val to = XY(x,y)
      import state.board
      import board.map
      map.find { case (_, piece) => piece.id == id } foreach { case (xy, gamePiece) =>
        val frozen = frozenFor(gamePiece.since)
        if (frozen > 0) {
          // Plan move
          gamePiece.plan.foreach { plan => broadcast(Unplan(plan.pid, moved = false)) }
          val newPlan =
            if (!gamePiece.moves(xy)(board.size).flatten.contains(to)) None
            else Plan(to).tap(p => broadcast(ClientPlan(p.pid, gamePiece.color, xy, p.pxy))).pipe(Some(_))
          val newPiece = gamePiece.copy(plan = newPlan)
          context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
          import context.dispatcher
          context.system.scheduler.scheduleOnce(Duration(frozen, TimeUnit.MILLISECONDS), self, ExecutePlan(id))
        } else {
          // Execute move
          // Remove any plan for the piece
          gamePiece.plan.foreach { plan => broadcast(Unplan(plan.pid, moved = plan.pxy == to)) }
          val steps = gamePiece.moves(xy)(board.size).find(_.contains(to))
          if (
            // Illegal move for this piece
            steps.isEmpty ||
            // Move illegal because a piece is in the way
            steps.exists(_.takeWhile(_ != to).exists(map.contains)) ||
            // Pawn may not move diagonally unless there's a piece there
            (gamePiece.piece == Pawn && xy.x != to.x && !map.contains(to)) ||
            // Pawn is blocked by a piece from moving forward
            (gamePiece.piece == Pawn && xy.x == to.x && map.contains(to)) ||
            // Can't capture own piece
            map.get(to).exists(_.color == gamePiece.color)
          ) {
            val newPiece = gamePiece.copy(plan = None)
            context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
          } else {
            map.get(to).foreach { gamePiece =>
              // remove any plan for a captured piece, then remove piece
              gamePiece.plan.foreach { plan => broadcast(Unplan(plan.pid, moved = plan.pxy == to)) }
              broadcast(Remove(gamePiece.id))
            }
            val newWinner = state.winner.orElse(
              if (map.get(to).exists(_.piece == King)) { broadcast(Winner(gamePiece.color)); Some(gamePiece.color) } else None
            )
            val newPiece =
              if (gamePiece.piece == Pawn && (y == 0 || y == board.size.y - 1))
                gamePiece.copy(plan = None, since = now, piece = Queen)
              else
                gamePiece.copy(plan = None, since = now)
            val newMap = map - xy + (to -> newPiece)
            broadcast(Move(newPiece.id, x, y, freeze(newPiece.since)))
            context.become(game(state.copy(winner = newWinner, board = board.copy(map = newMap))))
          }
        }
      }
    case m =>
      log.info(s"received $m")
  }
  def send[T: Encoder](to: ActorRef)(msg: T): Unit = to ! WSHandler.ForWS(msg.asJson.noSpaces)
  def freeze(since: Long): Long = math.max(math.max(0, initialFreezeUntil - now), since + freeze - now)
  def frozenFor(since: Long): Long = math.max(0L, math.max(initialFreezeUntil, since + freeze) - now)
}
