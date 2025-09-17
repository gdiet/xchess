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
  extension (p: Piece) def value  : Char    = p
  extension (p: Piece) def isWhite: Boolean = p.isUpper
  extension (p: Piece) def isPawn : Boolean = p == 'P' || p == 'p' || p == 'M' || p == 'm'
  extension (p: Piece) def promote: Piece   = { assert(p.toUpper == 'M'); if p.isUpper then 'Q' else 'q' }
  extension (p: Piece) def moved  : Piece   = p match
    case 'P' => 'M' // white pawn moved
    case 'p' => 'm' // black pawn moved
    case _   =>  assert(false); p

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
given Ordering[GameTime] with
  def compare(x: GameTime, y: GameTime): Int = x.compare(y)
object GameTime:
  def apply(value: Long): GameTime = value
  def max(t1: GameTime, t2: GameTime): GameTime = if t1 > t2 then t1 else t2
  extension (t: GameTime) def value: Long = t
  extension (t: GameTime) def whiteFirst: Boolean = t % 2 == 0
  extension (t: GameTime) def > (other: GameTime): Boolean = t > other // Note: Could use Ordering instead
  extension (t: GameTime) def <= (other: GameTime): Boolean = t <= other // Note: Could use Ordering instead
  extension (t: GameTime) def + (increment: Long): GameTime = t + increment

opaque type PlanId = Long
object PlanId:
  def zero: PlanId = 0
  extension (p: PlanId) def + (step: Int) : PlanId = p + step

type Square = (col: Col, row: Row)
object Square:
  def apply(string: String): Square = (col = string.head - 'A', row = string.tail.toInt - 1)
  extension (s: Square) def string: String = s"${('A' + s.col).toChar}${s.row + 1}"
  extension (s: Square) def + (cols: Int, rows: Int): Square = (s.col + cols, s.row + rows)
  extension (s: Square) def notNegative: Boolean = s.col >= 0 && s.row >= 0
  extension (s: Square) def <= (other: Square): Boolean = s.col <= other.col && s.row <= other.row
  extension (s: Square) def contains(other: Square): Boolean = other.notNegative && other <= s

type PieceOnMap = (piece: Piece, frozenUntil: GameTime)
