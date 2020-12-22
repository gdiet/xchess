package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props}
import akka.http.javadsl.model.StatusCodes

object GameRegistry {
  def apply(): Props = Props(new GameRegistry)
}
class GameRegistry extends Actor with ActorLogging {
  override def preStart(): Unit = log.info(s"Started")
  override def postStop(): Unit = log.debug(s"Stopped")
  override def receive: Receive = withGames(Map())
  def withGames(games: Map[String, ActorRef]): Receive = {

    case PostGameBody(name, gameType, initialFreeze, freeze) =>
      if (games.contains(name)) sender() ! StatusCodes.CONFLICT
      else GameActor(name, gameType, initialFreeze, freeze) match {
        case Left(statusCode) => sender() ! statusCode
        case Right(props) => context.become(withGames(games + (name -> context.actorOf(props)))); sender() ! StatusCodes.CREATED
      }

    case gameName: String =>
      sender() ! games.get(gameName)

    case m =>
      log.error(s"Received unexpected $m from ${sender()}")
  }
}
