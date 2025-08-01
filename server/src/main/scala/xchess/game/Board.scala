package xchess.game

import scala.collection.immutable.ListMap

def firstScheduledMove(board: Board, plannedMoves: ListMap[Square, Square], gameTime: GameTime): Option[(Square, Square)] =
  plannedMoves.collectFirst { case (from, to) if board.map.get(from).exists(_.frozenUntil <= gameTime) => (from, to) }

/** @return the target square if the move might be valid, None if not.
 *          "Might be valid": On the target square might be a piece of the same color.
 *          Does not check whether moves are beyond borders of the board. */
def tryMove(board: Board, from: Square, to: Square): Option[Square] = {
  board.map.get(from) match {
    case None => None // no piece at square: should not happen
    case Some((piece, _)) =>
      val rows = to.row - from.row // difference in rows (vertical movement, e.g. 1 -> 2)
      val cols = to.col - from.col // difference in cols (horizontal movement, e.g. a -> b)

      def pawnMoveOneLogic: Option[Square] =
        if Math.abs(rows) != 1 then None // must move exactly one row
        else if rows < 0 == piece.isWhite then None // must move in the right direction
        else if Math.abs(cols) > 1 then None // must not move more than one col
        else if cols == 0 && board.map.contains(to) then None // must not capture a piece on the same col
        else if Math.abs(cols) == 1 && !board.map.contains(to) then None // must capture a piece if moving diagonally
        else Some(to) // yay

      piece.value.toUpper match {
        case 'K' => // King move logic
          if Math.abs(rows) > 1 || Math.abs(cols) > 1 then None // must not move more than one square in any direction
          else Some(to)
        case 'Q' => // Queen move logic
          if Math.abs(rows) != Math.abs(cols) && rows != 0 && cols != 0 then None // must move in a straight line or diagonally
          else ???
        case 'B' => // Bishop move logic
          if Math.abs(rows) != Math.abs(cols) then None // must move diagonally
          else ???
        case 'N' => // Knight move logic
          if Math.abs(rows) * Math.abs(cols) != 2 then None // must move in an L-shape
          else Some(to)
        case 'R' => // Rook move logic
          if rows != 0 && cols != 0 then None // must move in a straight line
          else ???
        case 'P' => // Pawn not moved logic
          if Math.abs(rows) <= 1 then pawnMoveOneLogic
          else if Math.abs(rows) > 2 then None // must not move more than two rows
          else if cols != 0 then None // must not move horizontally when advancing two rows
          else ???
        case 'M' => // Pawn already moved logic
          pawnMoveOneLogic
        case _   => throw new IllegalArgumentException(s"Unknown piece type: $piece")
      }
  }
}

case class Board(size: Square, map: Map[Square, MapEntry])

def createBoard(name: String, initialFreezeUntil: GameTime = GameTime(3000)): Board = {
  val lines = boardLayout(name).linesIterator.toSeq
  val size = (col = Col(lines.head.length), row = Row(lines.length))
  val pieces = for {
    (line,  y) <- lines.zipWithIndex
    (piece, x) <- line .zipWithIndex
    if piece != '+'
  } yield (Col(x), Row(y)) -> (Piece(piece), initialFreezeUntil)
  Board (size, pieces.toMap)
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
