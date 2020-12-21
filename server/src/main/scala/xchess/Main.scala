package xchess

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout

import java.util.concurrent.TimeUnit
import scala.util.Success
import scala.util.chaining.scalaUtilChainingOps

object Main extends App with ClassLogging {
  implicit val system: ActorSystem = ActorSystem("server")
  implicit val askTimeout: Timeout = Timeout(1, TimeUnit.SECONDS)
  val gameRegistry = system.actorOf(GameRegistry())
  val route: Route = concat(
    path("ws" / Segment) { gameName =>
      onComplete(gameRegistry.ask(gameName)) {
        case Success(Some(game: ActorRef)) => handleWebSocketMessages(WSHandler(game))
        case _ => complete(StatusCodes.NotFound -> "Game not found.")
      }
    },
    get { complete("This is xchess.") }
  )
  Http()(system).newServerAt("localhost", 8080).bind(route)
  println(s"xchess server online at http://localhost:8080/")
  Thread.sleep(Long.MaxValue)
}
