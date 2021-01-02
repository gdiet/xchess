package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCode
import io.circe.Encoder
import io.circe.generic.auto._
import io.circe.syntax._
import xchess.GameActor._
import xchess.logic.Board

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
      state.board.map.foreach { case xy -> piece => send(Add(piece.id, xy.x, xy.y, piece.color, piece.name, freeze(piece.since))) }
      state.winner.foreach(winner => send(Winner(winner)))
      // TODO send planned moves
    case ClientDisconnected =>
      context.become(game(state.copy(clients = state.clients - sender())))
    case As(m @ ClientMove(id, x, y)) =>
      log.info(s"received $m")
    case m =>
      log.info(s"received $m")
  }
  def send[T: Encoder](msg: T): Unit = sender() ! WSHandler.ForWS(msg.asJson.noSpaces)
  def freeze(since: Long): Long = math.max(math.max(0, initialFreezeUntil - now), since + freeze - now)
  def frozen(since: Long): Boolean = now < initialFreezeUntil || now < since + freeze
}
