package xchess

import cask.model.Response
import cask.model.Response.Data
import ujson.{Obj, Value}

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}
import scala.annotation.tailrec

object Server extends cask.MainRoutes:
  override def port = 7070
  //  override def host = "0.0.0.0" // to allow access from other machines
  initialize()
  println(s"xChess server started at port $port")

  private val webRoot: Path = Paths.get("src/main/resources")
  private val games: GameRegistry = GameRegistry()

  // API routes
  @cask.post("/api/games")
  def postGame(id: Option[String]): Response[Value] =
    games.newGame(id) match
      case Some(gameId) =>
        Response(ujson.Obj("id" -> gameId, "msg" -> "Game created successfully"), 201)
      case None =>
        Response(ujson.Value("Failed to create game: ID conflict or too many games"), 409)

  // Websockets
  @cask.websocket("/ws/:gameId")
  def websockets(gameId: String): cask.WebsocketResult = {
    games.game(gameId) match
      case None => Response("Game not found", 404)
      case Some(game) =>
        cask.WsHandler { ws =>
          game.subscribe(new Subscription {
            override def message(message: String): Unit = cask.Ws.Text(message)
            override def close(): Unit = ws.send(cask.Ws.Close())
          })
          cask.WsActor { case cask.Ws.Text(message) => game.receiveMessage(message) }
        }
  }

  // Web routes
  @cask.get("/:path", subpath = true)
  def webRoutes(ctx: cask.Request, path: Seq[String]): Response[Data] =
    serveStaticFile(path ++ ctx.remainingPathSegments)

  @cask.get("")
  def rootRoute(): Response[Data] =
    serveStaticFile(Seq())

  @tailrec
  private def serveStaticFile(path: Seq[String]): Response[Data] =
    val filePath = path.foldLeft(webRoot)(_.resolve(_))
    if Files.isDirectory(filePath) then
      serveStaticFile(path :+ "index.html")
    else if Files.isRegularFile(filePath) then
      val contentType = Option(Files.probeContentType(filePath)).getOrElse("application/octet-stream")
      Response(java.nio.file.Files.newInputStream(filePath): Data, 200, Seq("Content-Type" -> contentType))
    else
      Response("": Data, 404)
