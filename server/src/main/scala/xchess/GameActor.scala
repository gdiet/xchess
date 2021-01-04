package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCode
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import xchess.GameActor._
import xchess.logic.{Board, Plan, XY}

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
      import state.board
      import board.map
      map.find { case (_, piece) => piece.id == id } foreach { case (xy, piece) =>
        if (frozen(piece.since)) {
          piece.plan.foreach { plan => send(Unplan(plan.pid, moved = false)) }
          val newPlan =
            if (!piece.moves(xy)(board.size).flatten.contains(XY(x,y))) None
            else Plan(XY(x,y)).tap(p => send(ClientPlan(p.pid, piece.color, xy, p.pxy))).pipe(Some(_))
          val newPiece = piece.copy(plan = newPlan)
          context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
        } else {
          // There is not supposed to be any plan because it's not frozen, yet let's make sure...
          piece.plan.foreach { plan => send(Unplan(plan.pid, moved = true)) }
          // TODO validate that move is legal
          // TODO execute actual move
          val newPiece = piece.copy(plan = None, since = now)
          val newMap = map - xy + (XY(x,y) -> newPiece)
          send(Move(piece.id, x, y))
          context.become(game(state.copy(board = board.copy(map = newMap))))
        }
      }
    case m =>
      log.info(s"received $m")
  }
  def send[T: Encoder](msg: T): Unit = sender() ! WSHandler.ForWS(msg.asJson.noSpaces)
  def freeze(since: Long): Long = math.max(math.max(0, initialFreezeUntil - now), since + freeze - now)
  def frozen(since: Long): Boolean = now < initialFreezeUntil || now < since + freeze
}
