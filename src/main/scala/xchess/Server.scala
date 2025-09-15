package xchess

import cask.*

object Server extends cask.Main with cask.Routes:
  override def host = "0.0.0.0" // allow access from other machines
  override def port = 7070
  val staticFiles: java.nio.file.Path = java.nio.file.Paths.get("src/main/resources")

  initialize()
  override def log: Logger = ServerLogger
  override def allRoutes: Seq[Routes] = Seq(this, StaticFilesAtWebRoot(staticFiles))
  println(s"xChess server started at port $port")

  val gameRegistry = xchess.game.GameRegistry()

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

  @cask.websocket("/ws/:gameId")
  def websockets(gameId: String): WebsocketResult =
    gameRegistry.game(gameId) match
      case None => Response(ujson.Obj("cause" -> "game not found"), 404)
      case Some(game) =>
        WsHandler { ws =>
          val subscription: xchess.game.Subscription = new xchess.game.Subscription:
            override def onNext(message: String): Unit = ws.send(Ws.Text(message))
            override def close(): Unit = ws.send(Ws.Close())
          game.subscribe(subscription)
          WsActor {
            case Ws.Text(message) => game.receiveMessage(message)
            case Ws.ChannelClosed() => game.unsubscribe(subscription)
          }
        }
