package xchess.game

import scala.collection.immutable.ListMap

def firstScheduledMove(board: Board, plannedMoves: ListMap[Square, Square], gameTime: Long): Option[(Square, Square)] =
  plannedMoves.collectFirst { case (from, to) if board.map.get(from).exists(_.frozenUntil <= gameTime) => (from, to) }

def tryMove(board: Board, from: Square, to: Square): Object =
  if (from == to) ???
  else board.map.get(from) match {
    case None => ???
    case Some((piece, _)) =>
      piece.value.toUpper match {
        case 'K' =>
          if (Math.abs(from.file - to.file) > 1 || Math.abs(from.rank - to.rank) > 1)
            ???
          else
            throw new IllegalArgumentException(s"Invalid king move from $from to $to")
          // King move logic
        case 'Q' => ??? // Queen move logic
        case 'B' => ??? // Bishop move logic
        case 'N' => ??? // Knight move logic
        case 'R' => ??? // Rook move logic
        case 'P' => ??? // Pawn not moved logic
        case 'M' => ??? // Pawn already moved logic
        case _   => throw new IllegalArgumentException(s"Unknown piece type: $piece")
      }
  }

case class Board(size: Square, map: Map[Square, MapEntry])

def createBoard(name: String, initialFreezeTime: Long = 3000): Board = {
  val lines = boardLayout(name).linesIterator.toSeq
  val size = (file = File(lines.head.length), rank = Rank(lines.length))
  val pieces = for {
    (line,  y) <- lines.zipWithIndex
    (piece, x) <- line .zipWithIndex
    if piece != '+'
  } yield (File(x), Rank(y)) -> (Piece(piece), initialFreezeTime)
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
