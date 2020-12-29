package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCode
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
      sender() ! WSHandler.ForWS(s"""{"x":${state.board.size.x},"y":${state.board.size.y}}""")
      state.board.map.foreach { case xy -> piece =>
        sender() ! WSHandler.ForWS(
          s"""{"cmd":"add","id":${piece.id},"x":${xy.x},"y":${xy.y},"color":"${piece.color}","piece":"${piece.name}","freeze":${freeze(piece.since)}}"""
        )
      }
      state.winner.foreach(winner => sender() ! WSHandler.ForWS(s"""{"winner":"$winner"}"""))
      // TODO send planned moves
      Thread.sleep(2000)
      sender() ! WSHandler.ForWS(s"""{"cmd":"move","id":13,"x":3,"y":5,"freeze": 5000}""")
      Thread.sleep(2000)
      sender() ! WSHandler.ForWS(s"""{"cmd":"remove","id":8}""")
    case ClientDisconnected =>
      context.become(game(state.copy(clients = state.clients - sender())))
    case m =>
      log.info(s"received $m")
  }
  def freeze(since: Long): Long = math.max(math.max(0, initialFreezeUntil - now), since + freeze - now)
}
