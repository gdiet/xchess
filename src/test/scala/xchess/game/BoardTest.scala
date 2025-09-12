package xchess.game

class BoardTest extends munit.FunSuite:
  val time = GameTime(1)
  val wBishop = (Piece('B'), time)
  val wPawn = (Piece('M'), time)
  val bPawn = (Piece('m'), time)

  test("bishop move blocked by enemy piece captures that piece") {
    Board(Square("H8"), Map(
      Square("C1") -> wBishop,
      Square("E3") -> bPawn,
    )).executeMove(Square("C1"), Square("G5"), time) match {
      case Some((newBoard, movedTo)) =>
        assertEquals(movedTo, Square("E3"))
        assertEquals(newBoard.map.size, 1)
        assertEquals(newBoard.map.get(Square("E3")), Some(wBishop))
      case None =>
        fail("move should be valid")
    }
  }

  test("bishop move blocked by own piece is not executed at all") {
    Board(Square("H8"), Map(
      Square("C1") -> wBishop,
      Square("E3") -> wPawn,
    )).executeMove(Square("C1"), Square("G5"), time) match {
      case Some((_, movedTo)) =>
        fail(s"move should not be valid, but target is $movedTo")
      case None =>
        // success
    }
  }
