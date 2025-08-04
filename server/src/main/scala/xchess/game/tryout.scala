package xchess.game

@main
def tryout(): Unit = {
  import upickle.default.*
//  implicit val rowRW: ReadWriter[Row] = readwriter[Int].bimap[Row](Row.value, Row(_))
//  implicit val colRW: ReadWriter[Col] = readwriter[Int].bimap[Col](Col.value, Col(_))
//  implicit val squareRW: ReadWriter[Square] = readwriter[(Col, Row)].bimap[Square](_.toTuple, t => t)
  implicit val squareRW: ReadWriter[Square] = readwriter[String].bimap[Square](Square.string, Square(_))
  implicit val timeRW: ReadWriter[GameTime] = readwriter[Long].bimap[GameTime](GameTime.value, GameTime(_))
  implicit val pieceRW: ReadWriter[Piece] = readwriter[Char].bimap[Piece](Piece.value, Piece(_))
  implicit val mapEntryRW: ReadWriter[MapEntry] = readwriter[(Piece, GameTime)].bimap[MapEntry](_.toTuple, t => t)
  implicit val mapRW: ReadWriter[Map[Square, MapEntry]] = readwriter[Map[String, MapEntry]].bimap[Map[Square, MapEntry]](
      m => m.map((k,v) => (Square.string(k), v)),
      m => m.map((k,v) => (Square(k), v))
    )
  implicit val boardRW: ReadWriter[Board] = macroRW[Board]
//  implicit val gameRW: ReadWriter[Game] = macroRW[Game]
  val board = createBoard("standard")
//  println(write(board))
  val game = Game(board)
  val moves = game.plannedMoves + (Square("B2") -> Square("B3"))
  println(write(moves))

//  case class Person(name: String, age: Int)
//  val alice = Person("Alice", 30)
//  implicit val personRW: ReadWriter[Person] = macroRW
//  val jsonString = write(alice)
//  implicit val gameRW: ReadWriter[Game] = macroRWAll
//  println(write(Game("standard")))
//  println(jsonString)

  //  val board = createBoard("standard")
//  val plannedMoves = ListMap(
//    "B2" -> "B3",
//    "C2" -> "C4",
//    "C1" -> "A3",
//    "A2" -> "A4",
//  ).map(Square(_) -> Square(_))
//  val game = Game(board, plannedMoves, 3000)
//  println(executeScheduledMoves(game, GameTime(3000)))
}
