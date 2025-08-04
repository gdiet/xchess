package xchess

import xchess.game.GameTime

class GameClock:
  private var offsetToUnixTime: Long = System.currentTimeMillis()
  private var stoppedAt: Option[Long] = Some(offsetToUnixTime)

  def stop(): Unit =  synchronized {
    if stoppedAt.isEmpty then stoppedAt = Some(System.currentTimeMillis())
  }

  def start(): Unit = synchronized {
    stoppedAt.foreach(stopped => {
      offsetToUnixTime += System.currentTimeMillis() - stopped
      stoppedAt = None
    })
  }

  def time: GameTime = synchronized {
    stoppedAt match
      case Some(stopped) => GameTime(stopped - offsetToUnixTime)
      case None => GameTime(System.currentTimeMillis() - offsetToUnixTime)
  }
