package xchess.game

import System.{currentTimeMillis => now}

// Use from synchronized context only
class GameClock(val millisPerTick: Long):
  private var offsetToUnixTime: Long = now
  private var stoppedAt: Option[Long] = Some(offsetToUnixTime)

  def isStopped: Boolean = stoppedAt.isDefined
  def stop(): Unit = if stoppedAt.isEmpty then stoppedAt = Some(now)
  def start(): Unit = stoppedAt.foreach { stopped => offsetToUnixTime += now - stopped; stoppedAt = None }
  def timeMillis: Long = stoppedAt.fold(now - offsetToUnixTime)(_ - offsetToUnixTime)
  def time: GameTime = GameTime(timeMillis / millisPerTick)

  def millisUntil(time: GameTime): Option[Long] =
    if stoppedAt.isDefined then None
    else Some(time.value * millisPerTick - (now - offsetToUnixTime))
