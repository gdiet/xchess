package xchess.game

import scala.collection.immutable.ListMap

@main
def tryout(): Unit = {
  val board = createBoard("standard")
  val plannedMoves = ListMap(
    Square("B2") -> Square("B3"),
    Square("C2") -> Square("C4"),
    Square("C1") -> Square("A3"),
    Square("A2") -> Square("A4")
  )
  val game = Game(board, plannedMoves, 3000)
  println(executeScheduledMoves(game, GameTime(3000)))
}
