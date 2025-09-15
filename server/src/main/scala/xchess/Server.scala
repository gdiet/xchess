package xchess

import cask.*
import cask.Response.Data
import ujson.{Obj, Value}
import xchess.game.GameHandler.Subscription
import xchess.game.GameRegistry

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}

object Server extends cask.MainRoutes:
  override def port = 7070
  //  override def host = "0.0.0.0" // to allow access from other machines
  initialize()
  println(s"xChess server started at port $port")

  private val webRoot: Path = Paths.get("src/main/resources")
  private val games: GameRegistry = GameRegistry()

  // API routes
  @cask.post("/api/games")
  def postGame(ctx: Request): Response[Value] = {
    val body = ujson.read(ctx.exchange.getInputStream).obj
    games.newGame(body.get("id").flatMap(_.strOpt)) match
      case Some(gameId) =>
        Response(ujson.Obj("id" -> gameId), 201)
      case None =>
        Response(ujson.Obj("cause" -> "ID conflict or too many games"), 409)
  }

  // Websockets
  @cask.websocket("/ws/:gameId")
  def websockets(gameId: String): WebsocketResult = {
    games.game(gameId) match
      case None => Response("Game not found", 404)
      case Some(game) =>
        WsHandler { ws =>
          val subscription = new Subscription {
            override def message(message: String): Unit = ws.send(Ws.Text(message))
            override def close(): Unit = ws.send(Ws.Close())
          }
          game.subscribe(subscription)
          WsActor {
            case Ws.Text(message) => game.receiveMessage(message)
            case Ws.ChannelClosed() => game.unsubscribe(subscription)
          }
        }
  }

  // Web routes
  @cask.get("/:path", subpath = true)
  def webRoutes(ctx: cask.Request, path: Seq[String], queryParams: QueryParams): Response[Data] =
    serveStaticFile(path ++ ctx.remainingPathSegments, queryParams)

  @cask.get("")
  def rootRoute(queryParams: QueryParams): Response[Data] =
    serveStaticFile(Seq(), queryParams)

  private def serveStaticFile(path: Seq[String], queryParams: QueryParams): Response[Data] =
    val filePath = path.foldLeft(webRoot)(_.resolve(_))
    if Files.isDirectory(filePath) then
      val paramPairs  = queryParams.value.flatMap((k,s) => s.map(v => s"$k=$v"))
      val paramString = mkString(paramPairs, "?", "&", "")
      val pathString  = mkString(path, "/", "/", "/index.html")
      Response("", 301, Seq("Location" -> (pathString + paramString)), Nil)
    else if Files.isRegularFile(filePath) then
      val contentType =
        if filePath.toFile.getName.endsWith(".mjs") then "text/javascript"
        else Option(Files.probeContentType(filePath)).getOrElse("application/octet-stream")
      Response(java.nio.file.Files.newInputStream(filePath): Data, 200, Seq("Content-Type" -> contentType))
    else
      Response("": Data, 404)

  private def mkString(items: Iterable[String], start: String, mid: String, end: String): String =
    if items.isEmpty then end else items.mkString(start, mid, end)
