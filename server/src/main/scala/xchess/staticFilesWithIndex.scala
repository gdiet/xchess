package xchess

import cask.endpoints.{QueryParamReader, StaticUtil}
import cask.model.Request
import cask.model.Response.Raw
import cask.router.{HttpEndpoint, Result}

type Endpoint = HttpEndpoint[String, Seq[String]]

/** Adapted version of [[cask.endpoints.staticFiles]] annotation that serves index.html for directories. */
class staticFilesWithIndex(val path: String, headers: Seq[(String, String)] = Nil) extends Endpoint:
  override val methods: Seq[String]                    = Seq("get")
  override def subpath                                 = true
  override def wrapPathSegment(s: String): Seq[String] = Seq(s)
  override type InputParser[T]                         = QueryParamReader[T]

  override def wrapFunction(ctx: Request, delegate: Delegate): Result[Raw] =
    delegate(ctx, Map()).map { t =>
      val leadingSlash = if (t.startsWith("/")) "/" else ""
      val path = leadingSlash + (cask.internal.Util.splitPath(t) ++ ctx.remainingPathSegments.flatMap(cask.internal.Util.splitPath))
        .filter(s => s != "." && s != "..")
        .mkString("/")
      val nioPath = java.nio.file.Paths.get(path)

      if java.nio.file.Files.isDirectory(nioPath) then
        val contentType = java.nio.file.Files.probeContentType(nioPath.resolve("/index.html"))
        cask.model.StaticFile(path + "/index.html", headers :+ ("Content-Type" -> contentType))
      else
        val contentType = java.nio.file.Files.probeContentType(nioPath)
        cask.model.StaticFile(path, headers :+ ("Content-Type" -> contentType))
    }
