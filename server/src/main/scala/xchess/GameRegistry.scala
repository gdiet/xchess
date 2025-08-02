package xchess

class GameRegistry {

  private var games: Map[String, GameHandler] = Map()
  
  /** @return None if the game was not created (ID conflict or too many games) */
  def newGame(id: Option[String]): Option[String] = synchronized {
    id match {
      case Some(gameId) if games.contains(gameId) => None // ID conflict
      case Some(gameId) if games.size >= 3 => None // too many games
      case Some(gameId) =>
        games += (gameId -> new GameHandler(this))
        Some(gameId)
      case None =>
        val newGameId = java.util.UUID.randomUUID().toString
        games += (newGameId -> new GameHandler(this))
        Some(newGameId)
    }
  }

}

class GameHandler(gameRegistry: GameRegistry)
