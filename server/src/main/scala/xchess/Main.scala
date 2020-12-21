package xchess

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.server.Directives._
import akka.http.scaladsl.server.Route

import scala.io.StdIn

object Main extends App {
  implicit val system: ActorSystem = ActorSystem("server")
  val mainActor = system.actorOf(MainActor())
  val route: Route = concat(
    path("ws") { handleWebSocketMessages(WSHandler(mainActor)) },
    get { complete("this is xchess") }
  )
  val bindingFuture = Http()(system).newServerAt("localhost", 8080).bind(route)
  println(s"xchess server online at http://localhost:8080/")
  StdIn.readLine()
  import system.dispatcher
  bindingFuture.flatMap(_.unbind()).onComplete(_ => system.terminate())
}
