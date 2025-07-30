package xchess

import cask.model.Response
import cask.model.Response.{Data, Raw}
import ujson.Obj

import java.io.InputStream
import java.nio.file.{Files, Path, Paths}
import scala.annotation.tailrec

object Server extends cask.MainRoutes:
  override def port = 7070
  initialize()
  println(s"xChess server started at port $port")

  private val webRoot: Path = Paths.get("src/main/resources")

  // API routes
  @cask.get("/api/hello")
  def apiRoutes(): Obj = ujson.Obj("msg" -> "Hello from API!")

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
