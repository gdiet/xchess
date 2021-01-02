package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCode
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import xchess.GameActor._
import xchess.logic.{Board, Plan, XY}

import java.lang.System.{currentTimeMillis => now}

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
        piece.plan.foreach { plan => send(ClientPlan(plan.pid, xy, plan.pxy)) }
      }
      state.winner.foreach(winner => send(Winner(winner)))
    case ClientDisconnected =>
      context.become(game(state.copy(clients = state.clients - sender())))
    case As(ClientMove(id, x, y)) =>
      import state.board
      import board.map
      map.find { case (_, piece) => piece.id == id } foreach { case (xy, piece) =>
        piece.plan.foreach { plan => send(Unplan(plan.pid)) }
        // TODO check whether still frozen
        // TODO validate that plan or move is legal
        val newPlan = if (xy == XY(x,y)) None else Some(Plan(XY(x,y)))
        newPlan.foreach { plan => send(ClientPlan(plan.pid, xy, plan.pxy)) }
        val newPiece = piece.copy(plan = newPlan)
        context.become(game(state.copy(board = board.copy(map = map + (xy -> newPiece)))))
      }
    case m =>
      log.info(s"received $m")
  }
  def send[T: Encoder](msg: T): Unit = sender() ! WSHandler.ForWS(msg.asJson.noSpaces)
  def freeze(since: Long): Long = math.max(math.max(0, initialFreezeUntil - now), since + freeze - now)
  def frozen(since: Long): Boolean = now < initialFreezeUntil || now < since + freeze
}
