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

  test("white pawn promotion while capturing") {
    Board(Square("H8"), Map(
      Square("G7") -> wPawn,
      Square("H8") -> bPawn,
    )).executeMove(Square("G7"), Square("H8"), time) match {
      case Some((newBoard, movedTo)) =>
        assertEquals(movedTo, Square("H8"))
        assertEquals(newBoard.map.size, 1)
        assertEquals(newBoard.map.get(Square("H8")), Some((Piece('Q'), time)))
      case None =>
        fail("move should be valid")
    }
  }

  test("black pawn promotion without capturing") {
    Board(Square("H8"), Map(
      Square("G2") -> bPawn,
      Square("H1") -> wBishop,
    )).executeMove(Square("G2"), Square("G1"), time) match {
      case Some((newBoard, movedTo)) =>
        assertEquals(movedTo, Square("G1"))
        assertEquals(newBoard.map.size, 2)
        assertEquals(newBoard.map.get(Square("G1")), Some((Piece('q'), time)))
      case None =>
        fail("move should be valid")
    }
  }
