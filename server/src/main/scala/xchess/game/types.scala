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
  extension (r: Piece) def moved: Piece = r match
    case 'P' => 'M' // white pawn moved
    case 'p' => 'm' // black pawn moved
    case _   =>  r

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
// FIXME remove unused lines
//  extension (r: GameTime) def < (other: GameTime): Boolean = r.value < other.value
  extension (r: GameTime) def > (other: GameTime): Boolean = r.value > other.value
  extension (r: GameTime) def + (timespan: Long): GameTime = r.value + timespan
//  extension (r: GameTime) def - (time: GameTime): Long     = r.value - time.value
//  given Ordering[GameTime] with
//    def compare(x: GameTime, y: GameTime): Int = x.compareTo(y)

type Square = (col: Col, row: Row)
object Square:
  def apply(string: String): Square = (col = string.head - 'A', row = string.tail.toInt - 1)
  extension (s: Square) def string: String = s"${('A' + s.col).toChar}${s.row + 1}"
  extension (s: Square) def +(cols: Int, rows: Int): Square = (s.col + cols, s.row + rows)

type MapEntry = (piece: Piece, frozenUntil: GameTime)
