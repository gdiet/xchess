package xchess

import akka.actor.{ActorRef, ActorSystem}
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.{StatusCode, StatusCodes}
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route
import akka.pattern.ask
import akka.util.Timeout
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport._
import io.circe.generic.auto._

import java.util.concurrent.TimeUnit
import scala.util.Success

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
    path("games") { post {
      // curl http://localhost:8080/games -X POST -d '{"name":"hallo","gameType":"chess","initialFreeze":60,"freeze":5}' -H "Content-Type: application/json"
      entity(as[PostGameBody]) { body => complete(gameRegistry.ask(body).mapTo[StatusCode]) }
    } },
    getFromDirectory("../client"),
    get { complete("This is xchess.") }
  )
  Http()(system).newServerAt("localhost", 8080).bind(route)
  println(s"xchess server online at http://localhost:8080/")
  Thread.sleep(Long.MaxValue)
}
