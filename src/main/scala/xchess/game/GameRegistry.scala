package xchess.game

class GameRegistry:
  private var games: Map[String, Game] = Map()
  newGame("test") // Default game for testing, might be removed later

  def newGame(id: String): Either[String, Unit] = synchronized {
    if games.size >= 3 then Left("Too many games")
    else if games.contains(id) then Left("Game already exists")
    else { games += (id -> Game()); Right(()) }
  }
  
  def game(id: String): Option[Game] = synchronized {
    games.get(id)
  }
