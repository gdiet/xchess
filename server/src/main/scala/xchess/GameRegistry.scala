package xchess

import akka.actor.{Actor, ActorLogging, ActorRef, Props, Terminated}
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
        case Right(props) =>
          val gameActor = context.actorOf(props)
          context.become(withGames(games + (name -> gameActor))); sender() ! StatusCodes.CREATED
          context.watch(gameActor)
      }

    case Terminated(gameActor) =>
      games.find(_._2 == gameActor).map(_._1).foreach { game =>
        context.become(withGames(games - game))
        log.info(s"Unregistered game '$game'")
      }

    case gameName: String =>
      sender() ! games.get(gameName)

    case m =>
      log.error(s"Received unexpected $m from ${sender()}")
  }
}
