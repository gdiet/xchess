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
  extension (r: Piece) def isPawn: Boolean = r == 'P' || r == 'p' || r == 'M' || r == 'm'
  extension (r: Piece) def promote: Piece = if r.isUpper then 'Q' else 'q'
  extension (r: Piece) def moved: Piece = r match
    case 'P' => 'M' // white pawn moved
    case 'p' => 'm' // black pawn moved
    case _   =>  r

opaque type Row = Int // rank 1 to 8 in standard chess, here 0 to 7 (more for large boards)
object Row:
  def apply(value: Int): Row = value
  extension (r: Row) def value: Int = r
  extension (r: Row) def - (other: Row): Int = r - other

opaque type Col = Int // file a to h in standard chess, here 0 to 7 (more for large boards)
object Col:
  def apply(value: Int): Col = value
  extension (c: Col) def value: Int = c
  extension (c: Col) def - (other: Col): Int = c - other

opaque type GameTime = Long // in arbitrary ticks, 0 is the start of the game
object GameTime:
  def apply(value: Long): GameTime = value
  extension (r: GameTime) def value: Long = r
  extension (r: GameTime) def > (other: GameTime): Boolean = r > other
  extension (r: GameTime) def + (timespan: Long): GameTime = r + timespan

type Square = (col: Col, row: Row)
object Square:
  def apply(string: String): Square = (col = string.head - 'A', row = string.tail.toInt - 1)
  extension (s: Square) def string: String = s"${('A' + s.col).toChar}${s.row + 1}"
  extension (s: Square) def + (cols: Int, rows: Int): Square = (s.col + cols, s.row + rows)

type PieceOnMap = (piece: Piece, frozenUntil: GameTime)
