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
}

class GameHandler(gameRegistry: GameRegistry)
