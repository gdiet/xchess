package xchess.game

import java.lang.System.currentTimeMillis as now

// Use from synchronized context only
class GameClock(val millisPerTick: Long):
  private var offsetToUnixTime: Long = now
  private var stoppedAt: Option[Long] = Some(offsetToUnixTime)

  def isStopped: Boolean = stoppedAt.isDefined
  def stop(): Boolean = { val result = stoppedAt.isEmpty; if result then stoppedAt = Some(now); result }
  def start(): Boolean = stoppedAt.tapEach { time => offsetToUnixTime += now - time; stoppedAt = None }.nonEmpty
  def timeMillis: Long = stoppedAt.fold(now - offsetToUnixTime)(_ - offsetToUnixTime)
  def time: GameTime = GameTime(timeMillis / millisPerTick)
  def millisUntil(time: GameTime): Long = time.value * millisPerTick - timeMillis
