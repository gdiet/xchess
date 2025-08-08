package xchess.game

import xchess.game.Square.*

case class Board(size: Square, map: Map[Square, MapEntry]) {
  override def toString: String = {
    val rows = for (r <- (0 until size.row.value).reverse) yield {
      val cols = for (c <- 0 until size.col.value) yield {
        map.get((Col(c), Row(r))) match {
          case Some((piece, _)) => piece.value
          case None => '+'
        }
      }
      cols.mkString("  ")
    }
    rows.mkString("\n", "\n", "\n")
  }
}

def createBoard(name: String, initialFreezeUntil: GameTime = GameTime(3000)): Board = {
  val lines = boardLayout(name).linesIterator.toSeq
  val size = (col = Col(lines.head.length), row = Row(lines.length))
  val pieces = for {
    (line, y) <- lines.zipWithIndex
    (piece, x) <- line.zipWithIndex
    if piece != '+'
  } yield (Col(x), Row(y)) -> (Piece(piece), initialFreezeUntil)
  // FIXME remove
  println(size)
  println(size.string)
  Board(size, pieces.toMap)
}

def executeMove(board: Board, from: Square, to: Square, freezeUntil: GameTime): Option[(board: Board, movedTo: Square)] = {
  board.map.get(from).flatMap { case (piece, _) =>
    tryMove(board, piece, from, to).flatMap(target =>
      board.map.get(target) match {
        case Some((targetPiece, targetFreezeUntil)) if targetPiece.isWhite == piece.isWhite =>
          None // piece of the same color at target, can not move there
        case _ =>
          Some(board.copy(map = board.map - from + (target -> (piece, freezeUntil))) -> target)
      }
    )
  }
}

/** @return the target square if the move might be valid, None if not.
 *          "Might be valid": On the target square might be a piece of the same color.
 *          Does not check whether moves are beyond borders of the board. */
def tryMove(board: Board, piece: Piece, from: Square, to: Square): Option[Square] = {
  val rows = to.row - from.row // difference in rows (vertical movement, e.g. 1 -> 2)
  val cols = to.col - from.col // difference in cols (horizontal movement, e.g. a -> b)

  def pawnMoveOneLogic: Option[Square] =
    if Math.abs(rows) != 1 then None // must move exactly one row
    else if rows < 0 == piece.isWhite then None // must move in the right direction
    else if Math.abs(cols) > 1 then None // must not move more than one col
    else if cols == 0 && board.map.contains(to) then None // must not capture a piece on the same col
    else if Math.abs(cols) == 1 && !board.map.contains(to) then None // must capture a piece if moving diagonally
    else Some(to) // yay

  /** @return the first occupied square in the direction of the move excluding the target square,
   *          or None if there is no such square. */
  def firstOccupiedSquare: Option[Square] =
    val rowStep = scala.math.signum(rows)
    val colStep = scala.math.signum(cols)
    @annotation.tailrec
    def startingAt(square: Square): Option[Square] =
      if square == to then None // don't include the target square
      else if board.map.contains(square) then Some(square) // found a piece
      else startingAt(square + (colStep, rowStep)) // continue searching
    startingAt(from + (colStep, rowStep))

  piece.value.toUpper match {
    case 'K' => // King move logic
      if Math.abs(rows) > 1 || Math.abs(cols) > 1 then None // can not move more than one square in any direction
      else Some(to)
    case 'Q' => // Queen move logic
      if Math.abs(rows) != Math.abs(cols) && rows != 0 && cols != 0 then None // must move in a straight line or diagonally
      else firstOccupiedSquare.orElse(Some(to))
    case 'B' => // Bishop move logic
      if Math.abs(rows) != Math.abs(cols) then None // must move diagonally
      else firstOccupiedSquare.orElse(Some(to))
    case 'N' => // Knight move logic
      if Math.abs(rows) * Math.abs(cols) != 2 then None // must move in an L-shape
      else Some(to)
    case 'R' => // Rook move logic
      if rows != 0 && cols != 0 then None // must move in a straight line
      else firstOccupiedSquare.orElse(Some(to))
    case 'P' => // Pawn not moved logic
      if Math.abs(rows) <= 1 then pawnMoveOneLogic
      else if Math.abs(rows) > 2 then None // can not move more than two rows
      else if cols != 0 then None // can not move horizontally when advancing two rows
      else if board.map.contains(to) then None // can not capture forward
      else if firstOccupiedSquare.isDefined then None // can not jump over pieces
      else Some(to)
    case 'M' => // Pawn already moved logic
      pawnMoveOneLogic
    case _   => throw new IllegalArgumentException(s"Unknown piece type: $piece")
  }
}

private def boardLayout(name: String): String = name match {
  case "large" =>
   """|RRNNBBQKQBBNNRR
      |RRNNBBQQQBBNNRR
      |PPPPPPPPPPPPPPP
      |PPPPPPPPPPPPPPP
      |+++++++++++++++
      |+++++++++++++++
      |+++++++++++++++
      |+++++++++++++++
      |+++++++++++++++
      |ppppppppppppppp
      |ppppppppppppppp
      |rrnnbbqqqbbnnrr
      |rrnnbbqkqbbnnrr""".stripMargin

  case _ =>
   """|RNBQKBNR
      |PPPPPPPP
      |++++++++
      |++++++++
      |++++++++
      |++++++++
      |pppppppp
      |rnbqkbnr""".stripMargin
}
