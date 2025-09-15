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

  /** { "id": "[random ID]", "boardLayout": "standard", "millisPerTick": 100, "freezeTicks": 50 }
    * all fields are optional, defaults as shown above
    *
    * 201 Created: { "id": "17" }
    * 409 Conflict: { "cause": "[message]" } */
  @cask.post("/api/games")
  def postGame(ctx: Request): Response[ujson.Value] =
    val body = ujson.read(ctx.exchange.getInputStream).obj
    val id = body.get("id").flatMap(_.strOpt).getOrElse(java.util.UUID.randomUUID().toString)
    game.GameOptions().withLayout(body.get("boardLayout").flatMap(_.strOpt))
      .withTickMillis(body.get("millisPerTick").flatMap(_.numOpt).map(_.toLong))
      .flatMap(_.withFreezeTicks(body.get("freezeTicks").flatMap(_.numOpt).map(_.toLong)))
      .flatMap(gameRegistry.newGame(id, _))
      .fold(
        failure => Response(ujson.Obj("cause" -> failure.cause), failure.statusCode),
        _ => Response(ujson.Obj("id" -> id), 201)
      )

  @cask.websocket("/ws/:gameId/:player")
  def websockets(gameId: String, player: String): WebsocketResult =
    gameRegistry.game(gameId) match
      case None => Response(ujson.Obj("cause" -> "game not found"), 404)
      case Some(game) =>
        WsHandler { ws =>
          val subscription: xchess.game.Subscription = new xchess.game.Subscription:
            override def isWhite: Boolean = player.toLowerCase().startsWith("w")
            override def send(message: String): Unit = ws.send(Ws.Text(message))
            override def close(): Unit = ws.send(Ws.Close())
          game.subscribe(subscription)
          WsActor {
            case Ws.Text(message) => game.receiveMessage(message)
            case Ws.ChannelClosed() => game.unsubscribe(subscription)
          }
        }
