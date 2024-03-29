package xchess.logic

object Board {
  def get(layout: String): String = layout match {
    case "large" => """|RRHHBBQKQBBHHRR
                       |RRHHBBQQQBBHHRR
                       |PPPPPPPPPPPPPPP
                       |PPPPPPPPPPPPPPP
                       |+++++++++++++++
                       |+++++++++++++++
                       |+++++++++++++++
                       |+++++++++++++++
                       |+++++++++++++++
                       |ppppppppppppppp
                       |ppppppppppppppp
                       |rrhhbbqqqbbhhrr
                       |rrhhbbqkqbbhhrr""".stripMargin

    case _       => """|RHBQKBHR
                       |PPPPPPPP
                       |++++++++
                       |++++++++
                       |++++++++
                       |++++++++
                       |pppppppp
                       |rhbqkbhr""".stripMargin
  }

  def apply(layout: String, initialFreeze: Long): Board = {
    val frozenUntil = System.currentTimeMillis() + initialFreeze
    val lines = get(layout).linesIterator.toSeq
    require(lines.nonEmpty && lines.map(_.length).distinct.size == 1)
    val positions = for {
      (line,y) <- lines.zipWithIndex
      (letter, x) <- line.zipWithIndex if letter != '+'
    } yield XY(x, y) -> letter
    val pieces = for {
      ((xy, letter), id) <- positions.zipWithIndex
      piece <- GamePiece.unapply(id, letter, frozenUntil)
    } yield xy -> piece
    Board(XY(lines.head.length, lines.size), pieces.size, pieces.toMap)
  }
}

case class Board(size: XY, nextId: Int, map: Map[XY, GamePiece])
