package xchess

class GameRegistry {
  private var games: Map[String, GameHandler] = Map()
  
  /** @return None if the game was not created (ID conflict or too many games) */
  def newGame(id: Option[String]): Option[String] = synchronized {
    if id.exists(games.contains) then None // ID conflict
    else if games.size >= 3 then None // too many games
    else
      val gameId = id.getOrElse(java.util.UUID.randomUUID().toString)
      games += (gameId -> new GameHandler(this))
      Some(gameId)
  }
  
  def game(gameId: String): Option[GameHandler] = synchronized {
    games.get(gameId)
  }
}

trait Subscription extends AutoCloseable {
  def message(message: String): Unit
}

class GameHandler(gameRegistry: GameRegistry) {
  private var chat: List[String] = List()
  private var subscriptions: Set[Subscription] = Set()
  
  def subscribe(subscription: Subscription): Unit = synchronized {
    // Subscribe to the game, e.g., for notifications or updates
    println("Subscribed to game")
    subscriptions += subscription
  }

  def unsubscribe(subscription: Subscription): Unit = synchronized {
    // Unsubscribe from the game
    println("Unsubscribed from game")
    subscriptions -= subscription
  }
  
  def receiveMessage(message: String): Unit = synchronized {
    // Handle incoming messages for the game
    println(s"Received message for game: $message")
    chat = chat :+ message
  }
}
