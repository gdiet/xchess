package xchess.game

import scala.collection.immutable.SortedMap

class PlannedMovesTest extends munit.FunSuite:
  val time = GameTime(2)
  val wBishop = (Piece('B'), time)
  val wPawn = (Piece('M'), time)
  val bPawn = (Piece('m'), time)

  test("move capures piece and cancels that piece's plan") {
    val board = Board(Square("H8"), Map(
      Square("C1") -> wBishop,
      Square("E3") -> bPawn,
    ))
    val stage1 = PlannedMoves(board, Map(), SortedMap(), Map(), PlanId.zero)
    val (stage2, time2) = stage1.plan(Square("C1"), Square("E3"), forWhite = true, GameTime(0)).get
    assertEquals(time2, time)
    val (stage3, time3) = stage2.plan(Square("E3"), Square("D3"), forWhite = false, time).get
    assertEquals(time3, time)
    val (stage4, executedMoves, removedPlans) = stage3.executePlans(time + 3, 3)
    assertEquals(executedMoves, Seq((Square("C1"), Square("E3"))))
    assertEquals(removedPlans, Seq((Square("C1"), true), (Square("E3"), false)))
    assertEquals(stage4.board.map.size, 1)
    assertEquals(stage4.board.map.get(Square("E3")), Some(Piece('B'), time + 3))
  }
