package xchess

import cask.*
import sourcecode.{File, Line, Text}
import ujson.Value

object Server extends cask.MainRoutes:
  override def port = 7070
  override def host = "0.0.0.0" // allow access from other machines
  override def log: Logger = new cask.Logger: // disable debug logging
    override def exception(t: Throwable): Unit = t.printStackTrace()
    override def debug(t: Text[Any])(implicit f: File, line: Line): Unit = {}

  initialize()
  println(s"xChess server started at port $port")

  @cask.post("/api/games")
  def postGame(ctx: Request): Response[Value] =
    val body = ujson.read(ctx.exchange.getInputStream).obj
    body.get("id").flatMap(_.strOpt) match
      case None => Response(ujson.Obj("cause" -> "Missing or invalid ID"), 400)
      case Some("17") => Response(ujson.Obj("id" -> "17"), 201)
      case Some(id) => Response(ujson.Obj("cause" -> "ID conflict or too many games"), 409)
