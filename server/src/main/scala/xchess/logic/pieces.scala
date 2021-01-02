package xchess.logic

import java.util.concurrent.atomic.AtomicInteger

case class Plan(pxy: XY, pid: Int = Plan.current.incrementAndGet() )
object Plan { private val current = new AtomicInteger() }

case class GamePiece(id: Int, piece: Piece, isWhite: Boolean, since: Long, plan: Option[Plan]) {
  def color: String = if (isWhite) "white" else "black"
  def name: String = piece.name
}

object GamePiece {
  private val allPieces = Seq(King, Queen, Bishop, Knight, Rook, Pawn)
  private val piecesMap = allPieces.map(p => p.letter -> p).toMap
  def unapply(id: Int, letter: Char, since: Long): Option[GamePiece] =
    piecesMap.get(letter.toUpper).map(GamePiece(id, _, letter.isUpper, since, None))
}

sealed trait Piece {
  def name: String = toString
  def letter: Char
  def moves(square: XY)(implicit board: XY): Seq[Seq[XY]]
  protected def direction(square: XY)(xy: (Int, Int))(implicit board: XY): Seq[XY] =
    Iterator.iterate(square + xy)(_ flatMap (_+ xy)).takeWhile(_.isDefined).flatten.toSeq
}

case object King extends Piece {
  def letter = 'K'
  def moves(xy: XY)(implicit board: XY): Seq[Seq[XY]] =
    Seq(
      (-1,-1),(0,-1),(1,-1),
      (-1, 0),       (1, 0),
      (-1, 1),(0, 1),(1, 1)
    ) flatMap (xy + _) map (Seq(_))
}

case object Queen extends Piece {
  def letter = 'Q'
  def moves(xy: XY)(implicit board: XY): Seq[Seq[XY]] =
    Bishop.moves(xy) ++ Rook.moves(xy)
}

case object Bishop extends Piece {
  def letter = 'B'
  def moves(xy: XY)(implicit board: XY): Seq[Seq[XY]] =
    Seq(
      (-1,-1),(1,-1),
      (-1, 1),(1, 1)
    ) map direction(xy) filterNot (_.isEmpty)
}

case object Knight extends Piece {
  def letter = 'H' // horse
  def moves(xy: XY)(implicit board: XY): Seq[Seq[XY]] =
    Seq(
              (-1,-2),(1,-2),
      (-2,-1),               (2,-1),
      (-2, 1),               (2, 1),
              (-1, 2),(1, 2)
    ) flatMap (xy + _) map (Seq(_))
}

case object Rook extends Piece {
  def letter = 'R'
  def moves(xy: XY)(implicit board: XY): Seq[Seq[XY]] =
    Seq(
             (0,-1),
      (-1,0),       (1,0),
             (0, 1)
    ) map direction(xy) filterNot (_.isEmpty)
}

case object Pawn extends Piece {
  def letter = 'P'
  def moves(square: XY)(implicit board: XY) =
    throw new UnsupportedOperationException
}
