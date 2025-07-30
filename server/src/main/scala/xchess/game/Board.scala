package xchess.game

/* K - King
 * Q - Queen
 * B - Bishop
 * N - Knight
 * R - Rook
 * P - Pawn, not yet moved
 * M - Pawn, already moved
 * 
 * Uppercase white, lowercase black */

type Plan = (from: Square, to: Square)
case class Game(board: Board, plannedMoves: Seq[Plan]) {
}

type Rank   = Int
type File   = Int
type Square = (file: File, rank: Rank)
type Piece  = Char
case class MapEntry(piece: Piece, frozenUntil: Long)
case class Board(size: Square, map: Map[Square, MapEntry])

object Board {
  def apply(name: String, initialFreezeTime: Long = 3000): Board = {
    val lines = layout (name).linesIterator.toSeq
    val size = (lines.head.length, lines.length)
    val pieces = for {
      (line, y) <- lines.zipWithIndex
      (piece, x) <- line.zipWithIndex
      if piece != '+'
    } yield (x, y) -> MapEntry (piece, initialFreezeTime)
    new Board (size, pieces.toMap)
  }

  private def layout(name: String): String = name match {
    case "large" => """|RRNNBBQKQBBNNRR
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

    case _ =>       """|RNBQKBNR
                       |PPPPPPPP
                       |++++++++
                       |++++++++
                       |++++++++
                       |++++++++
                       |pppppppp
                       |rnbqkbnr""".stripMargin
  }

}
