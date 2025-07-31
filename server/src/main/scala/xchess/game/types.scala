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

opaque type Rank = Int // 1 to 8 in standard chess, here 0 to 7 (more for large boards)
object Rank:
  def apply(value: Int): Rank = value
  extension (r: Rank) def value: Int = r
  extension (r: Rank) def - (other: Rank): Int = r.value - other.value

opaque type File = Int // a to h in standard chess, here 0 to 7 (more for large boards)
object File:
  def apply(value: Int): File = value
  extension (r: File) def value: Int = r
  extension (r: File) def - (other: File): Int = r.value - other.value

opaque type GameTime = Long // in milliseconds, 0 is the start of the game
object GameTime:
  def apply(value: Long): GameTime = value
  extension (r: GameTime) def value: Long = r
  extension (r: GameTime) def <=(other: GameTime): Boolean = r.value <= other.value

type Square = (file: File, rank: Rank)

type MapEntry = (piece: Piece, frozenUntil: GameTime)

