package xchess

import akka.actor.{Actor, ActorLogging, Props}

object GameRegistry {
  def apply(): Props = Props(new GameRegistry)
}
class GameRegistry extends Actor with ActorLogging {
  override def preStart(): Unit = log.info(s"Started")
  override def postStop(): Unit = log.debug(s"Stopped")
  override def receive: Receive = {
    case m =>
      log.info(s"received $m, reply to $sender")
      sender ! Some(self)
  }
}
