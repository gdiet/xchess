package xchess.game

class GameRegistry:
  private var games: Map[String, Game] = Map()
  newGame("test") // Default game for testing, might be removed later

  def newGame(id: String): Either[String, Unit] = synchronized {
    if games.size >= 3 then Left("too many games")
    else if games.contains(id) then Left("game already exists")
    else { games += (id -> Game()); Right(()) }
  }
  
  def game(id: String): Option[Game] = synchronized {
    games.get(id)
  }
