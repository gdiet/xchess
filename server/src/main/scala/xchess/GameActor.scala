package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCode
import xchess.GameActor._
import xchess.logic.Board

object GameActor {
  def apply(name: String, gameType: String, initialFreeze: Int, freeze: Int): Either[StatusCode, Props] =
    // TODO validate game settings
    Right(Props(new GameActor(name, GameState(Board(gameType)), initialFreeze, freeze)))

  case object ClientConnected
  case object ClientDisconnected
  case class FromClient(message: String)

  private case class GameState(board: Board, clients: Set[ActorRef] = Set())
}
class GameActor(name: String, initialState: GameState, initialFreeze: Int, freeze: Int) extends Actor with ActorLogging {
  override def preStart(): Unit = log.info(s"'$name' Started")
  override def postStop(): Unit = log.debug(s"'$name' Stopped")
  override def receive: Receive = game(initialState)
  def game(state: GameState): Receive = {
    case ClientConnected =>
      context.become(game(state.copy(clients = state.clients + sender())))
      sender() ! WSHandler.ForWS(s"""{"x":${state.board.size.x},"y":${state.board.size.y}}""")
      state.board.map.foreach { case xy -> piece =>
        sender() ! WSHandler.ForWS(
          s"""{"cmd":"add","id":${piece.id},"x":${xy.x},"y":${xy.y},"color":"${piece.color}","piece":"${piece.name}","freeze":${freeze(piece.since)}}"""
        )
      }
      // TODO send planned moves and winner information
    case ClientDisconnected =>
      context.become(game(state.copy(clients = state.clients - sender())))
    case m =>
      log.info(s"received $m")
  }
  def freeze(since: Long): Long = {
    val time = System.currentTimeMillis()
    math.max(math.max(0, since + initialFreeze - time), since + freeze - time)
  }
}
