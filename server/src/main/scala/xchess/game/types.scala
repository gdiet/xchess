package xchess.game

/** K - King
 * Q - Queen
 * B - Bishop
 * N - Knight
 * R - Rook
 * P - Pawn, not yet moved
 * M - Pawn, already moved
 *
 * Uppercase white, lowercase black */
opaque type Piece = Char
object Piece:
  def apply(value: Char): Piece = value
  extension (r: Piece) def value: Char = r
  extension (r: Piece) def isWhite: Boolean = r.isUpper

opaque type Row = Int // 1 to 8 in standard chess, here 0 to 7 (more for large boards)
object Row:
  def apply(value: Int): Row = value
  extension (r: Row) def value: Int = r
  extension (r: Row) def - (other: Row): Int = r.value - other.value

opaque type Col = Int // a to h in standard chess, here 0 to 7 (more for large boards)
object Col:
  def apply(value: Int): Col = value
  extension (r: Col) def value: Int = r
  extension (r: Col) def - (other: Col): Int = r.value - other.value

opaque type GameTime = Long // in milliseconds, 0 is the start of the game
object GameTime:
  def apply(value: Long): GameTime = value
  extension (r: GameTime) def value: Long = r
  extension (r: GameTime) def <=(other: GameTime): Boolean = r.value <= other.value

type Square = (col: Col, row: Row)

type MapEntry = (piece: Piece, frozenUntil: GameTime)
