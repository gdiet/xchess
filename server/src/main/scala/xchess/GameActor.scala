package xchess

import akka.actor.{Actor, ActorLogging, Props}
import akka.http.scaladsl.model.StatusCode

object GameActor {
  def apply(name: String, gameType: String, initialFreeze: Int, freeze: Int): Either[StatusCode, Props] = {
    // TODO validate game settings
    Right(Props(new GameActor(name, gameType, initialFreeze, freeze)))
  }
}
class GameActor(name: String, gameType: String, initialFreeze: Int, freeze: Int) extends Actor with ActorLogging {
  override def preStart(): Unit = log.info(s"'$name' Started")
  override def postStop(): Unit = log.debug(s"'$name' Stopped")
  override def receive: Receive = {
    case m =>
      log.info(s"received $m, reply to ${sender()}")
      sender() ! "OK"
  }
}
