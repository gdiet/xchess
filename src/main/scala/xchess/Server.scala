package xchess

import cask.*
import sourcecode.*

import java.nio.file.{Files, Path, Paths}

object Server extends cask.MainRoutes:
  // Server configuration
  override def host = "0.0.0.0" // allow access from other machines
  override def port = 7070
  val webRoot: Path = Paths.get("src/main/resources")

  // Logging
  override def log: Logger = new cask.Logger: // disable debug logging
    override def exception(t: Throwable): Unit = t.printStackTrace()
    override def debug(t: Text[Any])(implicit f: File, line: Line): Unit = {}

  // Server startup
  initialize()
  println(s"xChess server started at port $port")

  // Server state
  val gameRegistry = game.GameRegistry()

  // REST routes

  /** { "id": "17" }
    * id: optional, if omitted a random ID is used
    *
    * 201 Created: { "id": "17" }
    * 409 Conflict: { "cause": "[message]" } */
  @cask.post("/api/games")
  def postGame(ctx: Request): Response[ujson.Value] =
    val body = ujson.read(ctx.exchange.getInputStream).obj
    val id = body.get("id").flatMap(_.strOpt).getOrElse(java.util.UUID.randomUUID().toString)
    gameRegistry.newGame(id) match
      case Right(_) => Response(ujson.Obj("id" -> id), 201)
      case Left(cause) => Response(ujson.Obj("cause" -> cause), 400)

  // Serve static files in web root
  import Response.Data

  @cask.get("")
  def rootRoute(queryParams: QueryParams): Response[Data] =
    serveStaticFile(Seq(), queryParams)

  @cask.get("/:path", subpath = true)
  def webRoutes(ctx: cask.Request, path: Seq[String], queryParams: QueryParams): Response[Data] =
    serveStaticFile(path ++ ctx.remainingPathSegments, queryParams)

  private def serveStaticFile(path: Seq[String], queryParams: QueryParams): Response[Data] =
    val filePath = path.foldLeft(webRoot)(_.resolve(_))
    if Files.isDirectory(filePath) then
      val paramPairs = queryParams.value.flatMap((k, s) => s.map(v => s"$k=$v"))
      val paramString = mkString(paramPairs, "?", "&", "")
      val pathString = mkString(path, "/", "/", "/index.html")
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
