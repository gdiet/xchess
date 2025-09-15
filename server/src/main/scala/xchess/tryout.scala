package xchess

import xchess.game.GameClock

@main
def tryout(): Unit =
  val gameClock = GameClock()
  println(gameClock.time)
  Thread.sleep(500)
  println(gameClock.time)
  gameClock.start()
  Thread.sleep(500)
  println(gameClock.time)
  Thread.sleep(500)
  println(gameClock.time)
  gameClock.stop()
  Thread.sleep(500)
  println(gameClock.time)
  Thread.sleep(500)
  println(gameClock.time)
  gameClock.start()
  Thread.sleep(500)
  println(gameClock.time)
  Thread.sleep(500)
  println(gameClock.time)
  gameClock.stop()
  Thread.sleep(500)
  println(gameClock.time)
  Thread.sleep(500)
  println(gameClock.time)
