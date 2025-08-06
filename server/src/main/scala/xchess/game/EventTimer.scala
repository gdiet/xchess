package xchess.game

import java.util.concurrent.{ScheduledExecutorService, ScheduledFuture, TimeUnit}

class EventTimer(scheduler: ScheduledExecutorService, turnDuration: Long, event: Runnable):
  private var schedule: Option[ScheduledFuture[?]] = None
  private val clock: GameClock = GameClock()
  export clock.{time, isStopped}
  
  def stop(): Unit = synchronized {
    clock.stop()
    schedule.foreach(_.cancel(false))
  }

  def start(): Unit = synchronized {
    clock.start()
    val remainder = clock.time.value % turnDuration
    val timeToNextTurn = if remainder == 0 then 0 else turnDuration - remainder
    schedule = Some(scheduler.scheduleAtFixedRate(event, timeToNextTurn, turnDuration, TimeUnit.MILLISECONDS))
  }
