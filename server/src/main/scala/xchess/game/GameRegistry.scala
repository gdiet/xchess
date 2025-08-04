package xchess.game

class GameRegistry {
  private var games: Map[String, GameHandler] = Map()
  
  /** @return None if the game was not created (ID conflict or too many games) */
  def newGame(id: Option[String]): Option[String] = synchronized {
    if id.exists(games.contains) then None // ID conflict
    else if games.size >= 3 then None // too many games
    else
      val gameId = id.getOrElse(java.util.UUID.randomUUID().toString)
      games += (gameId -> GameHandler())
      Some(gameId)
  }

  def game(gameId: String): Option[GameHandler] = synchronized {
    games.get(gameId)
  }
}

trait Subscription extends AutoCloseable {
  def message(message: String): Unit
}

class GameHandler {
  private val clock: GameClock = GameClock()
  private var chat: List[String] = List()
  private var subscriptions: Set[Subscription] = Set()
  private var game: Game = Game("standard")

//  private val scheduler = Executors.newScheduledThreadPool(1)
//  scheduler.schedule(
//    new Runnable { override def run(): Unit = println("scheduled") }, 3000, java.util.concurrent.TimeUnit.MILLISECONDS
//  )//.cancel(false)

  def subscribe(subscription: Subscription): Unit = synchronized {
    subscriptions += subscription
    chat.reverse.foreach(message => subscription.message(s"chat: $message"))
  }

  def unsubscribe(subscription: Subscription): Unit = synchronized {
    subscriptions -= subscription
  }

  def receiveMessage(message: String): Unit = synchronized {
    message.split(":", 2) match {
      case Array("stop") => clock.stop(); println(s"clock stopped at ${clock.time}")
      case Array("start") => clock.start(); println(s"clock started at ${clock.time}")
      case Array("move", move) => println(s"move: $move")
      case Array("chat", message) =>
        chat = message :: chat.take(4)
        subscriptions.foreach(_.message(s"chat:$message"))
      case _ => println(s"WARNING - unknown command: $message")
    }
  }
}
