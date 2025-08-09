package xchess.game

class GameRegistry {
  private var games: Map[String, GameHandler] = Map("17" -> GameHandler()) // FIXME pre-created game for testing
  
  /** @return None if the game was not created (ID conflict or too many games) */
  def newGame(id: Option[String]): Option[String] = synchronized {
    if games.size >= 3 then None // too many games
    else
      val gameId = id.getOrElse(java.util.UUID.randomUUID().toString)
      if games.contains(gameId) then None // ID conflict
      else
        games += (gameId -> GameHandler())
        Some(gameId)
  }

  def game(gameId: String): Option[GameHandler] = synchronized {
    games.get(gameId)
  }
}
