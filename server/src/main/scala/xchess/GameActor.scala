package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.scaladsl.model.StatusCode

object GameActor {
  def apply(name: String, gameType: String, initialFreeze: Int, freeze: Int): Either[StatusCode, Props] = {
    // TODO validate game settings
    Right(Props(new GameActor(name, gameType, initialFreeze, freeze)))
  }
  case object ClientConnected
  case object ClientDisconnected
  case class FromClient(message: String)
  private case class GameState(
    clients: Set[ActorRef] = Set()
  )
}
class GameActor(name: String, gameType: String, initialFreeze: Int, freeze: Int) extends Actor with ActorLogging {
  import GameActor._
  override def preStart(): Unit = log.info(s"'$name' Started")
  override def postStop(): Unit = log.debug(s"'$name' Stopped")
  override def receive: Receive = game(GameState())
  def game(state: GameState): Receive = {
    case ClientConnected =>
      context.become(game(state.copy(clients = state.clients + sender())))
      sender() ! WSHandler.ForWS("""{"x":10,"y":8}""") // TODO send actual board size
      // TODO send game state
      // TODO remove example code
      sender() ! WSHandler.ForWS("""{"cmd":"add","id":1,"x":0,"y":1,"color":"white","piece":"rook","freeze":100}""")
      sender() ! WSHandler.ForWS("""{"cmd":"add","id":2,"x":2,"y":1,"color":"white","piece":"rook","freeze":100}""")
    case ClientDisconnected =>
      context.become(game(state.copy(clients = state.clients - sender())))
    case m =>
      log.info(s"received $m")
  }
}
