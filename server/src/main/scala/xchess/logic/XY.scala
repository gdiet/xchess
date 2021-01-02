package xchess.logic

/** Also used as JSON codec. */
case class XY(x: Int, y: Int) {
  def + (other: (Int, Int))(implicit board: XY): Option[XY] = other match { case (dx, dy) =>
    if (x+dx < board.x && x+dx >= 0 && y+dy < board.y && y+dy >= 0) Some(XY(x+dx, y+dy)) else None
  }
}
