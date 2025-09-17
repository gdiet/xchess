package xchess.game

import java.util.concurrent.Executors

class GameRegistry:
  private val scheduler = Executors.newScheduledThreadPool(Runtime.getRuntime.availableProcessors())
  private var games: Map[String, Game] = Map()
  newGame("test", GameOptions()) // Default game for testing, might be removed later

  def newGame(id: String, options: GameOptions): Either[SetupFailure, Unit] = synchronized {
    if games.size >= 3 then Left(Conflict("too many games"))
    else if games.contains(id) then Left(Conflict("game already exists"))
    else { games += (id -> Game(id, options, scheduler)); Right(()) }
  }
  
  def game(id: String): Option[Game] = synchronized {
    games.get(id)
  }
