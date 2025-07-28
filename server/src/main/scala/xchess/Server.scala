package xchess

import java.nio.file.{Files, Paths}

object Server extends cask.MainRoutes:

  override def port = 7070

// FIXME doesn't work:
//
//  @cask.get("/api")
//  def api() = "api"
//
//  @staticFilesWithIndex("/")
//  def staticFiles() = "src/main/resources"

// FIXME chatgpt proposal:

  // 1. API-Routen
  @cask.get("/api/hello")
  def hello() = ujson.Obj("msg" -> "Hello from API!")

  // 3. Catch-all Route für alle anderen Routen außer /api
  @cask.get("/:path", subpath = true)
  def fallback(ctx: cask.Request, path: Seq[String]) = {
    val fullPath = "/" + path.mkString("/")
    if (fullPath.startsWith("/api") || fullPath.startsWith("/assets")) {
      cask.Response("Not Found", statusCode = 404)
    } else {
      val indexPath = Paths.get("src/main/resources/index.html")
      if (Files.exists(indexPath)) {
        val content = Files.readString(indexPath)
        cask.Response(content, headers = List("Content-Type" -> "text/html"))
      } else {
        cask.Response("index.html not found", statusCode = 500)
      }
    }
  }

  initialize()
  println(s"xChess server started at port $port")
