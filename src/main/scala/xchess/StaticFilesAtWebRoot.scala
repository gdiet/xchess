package xchess

import cask.*
import cask.Response.Data

import java.nio.file.{Files, Path}

class StaticFilesAtWebRoot(staticFiles: Path) extends Routes:
  initialize()

  @cask.get("")
  def rootRoute(queryParams: QueryParams): Response[Data] =
    serveStaticFile(Seq(), queryParams)

  @cask.get("/:path", subpath = true)
  def webRoutes(ctx: cask.Request, path: Seq[String], queryParams: QueryParams): Response[Data] =
    serveStaticFile(path ++ ctx.remainingPathSegments, queryParams)

  private def serveStaticFile(path: Seq[String], queryParams: QueryParams): Response[Data] =
    val filePath = path.foldLeft(staticFiles)(_.resolve(_))
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
