package xchess.game

import scala.collection.immutable.ListMap

@main
def tryout(): Unit = {
  val board = createBoard("standard")
  val plannedMoves = ListMap(
    "B2" -> "B3",
    "C2" -> "C4",
    "C1" -> "A3",
    "A2" -> "A4",
  ).map(Square(_) -> Square(_))
  val game = Game(board, plannedMoves, 3000)
  println(executeScheduledMoves(game, GameTime(3000)))
}
