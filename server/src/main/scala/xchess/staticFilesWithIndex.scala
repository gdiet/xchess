package xchess

import cask.endpoints.{QueryParamReader, StaticUtil}
import cask.model.Request
import cask.router.HttpEndpoint

class staticFilesWithIndex(val path: String, headers: Seq[(String, String)] = Nil) extends HttpEndpoint[String, Seq[String]] {
  val methods = Seq("get")
  type InputParser[T] = QueryParamReader[T]

  override def subpath = true

  def wrapFunction(ctx: Request, delegate: Delegate) = {
    delegate(ctx, Map()).map { t =>
      val (path, contentTypeOpt) = StaticUtil.makePathAndContentType(t, ctx)
      println(s"${ctx.exchange.getRequestPath} -> $path")
      if java.nio.file.Files.isDirectory(java.nio.file.Paths.get(path)) then {
        val contentType = java.nio.file.Files.probeContentType(java.nio.file.Paths.get(path + "/index.html"))
        cask.model.StaticFile(path + "/index.html", headers :+ ("Content-Type" -> contentType))
      } else
        cask.model.StaticFile(path, headers ++ contentTypeOpt.map("Content-Type" -> _))
    }
  }

  def wrapPathSegment(s: String): Seq[String] = Seq(s)
}
