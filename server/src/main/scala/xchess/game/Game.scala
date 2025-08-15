package xchess.game

import xchess.util.{!!!, interleave}

import scala.collection.immutable.ListMap

case class Game(board: Board, plannedMoves: ListMap[Square, Square] = ListMap.empty, freezeTime: Long = 3000)
object Game:
  def apply(boardLayout: String): Game = Game(Board(boardLayout))

def executeScheduledMoves(game: Game, time: GameTime): (Game, Seq[(Square, Square)]) =
  import game.*
  val (laterMoves, currentMoves) = plannedMoves.partitionMap { (from, to) =>
    board.map.get(from) match
      case None => !!!(Right(None)) // piece not found, move will be removed. should not happen
      case Some((piece, frozenUntil)) if frozenUntil > time => Left(from -> to) // piece is frozen, skip this move
      case Some((piece, _)) => Right(Some((piece = piece, from = from, to = to))) // schedule the move
  }
  currentMoves.flatten.headOption match
    case Some(move) =>
      val whiteFirst = move.piece.isWhite // the player who has scheduled the first move is the one who plays first
      val (firstMoves, secondMoves) = currentMoves.flatten.partition(_.piece.isWhite == whiteFirst)
      val movesToExecute = interleave(firstMoves, secondMoves) // interleave the moves of both players
      var movesExecuted: Seq[(Square, Square)] = Seq()
      val newBoard = movesToExecute.foldLeft(board) { case (board, (piece, from, to)) =>
        tryMove(board, piece, from, to) match
          case None => board // move not valid, do not change the board. happens if a planned move is blocked
          case Some(target) =>
            movesExecuted = movesExecuted :+ (from -> target)
            board.copy(map = board.map - from + (target -> (piece.moved, time + freezeTime))) // execute the move
      }
      copy(board = newBoard, plannedMoves = ListMap.from(laterMoves)) -> movesExecuted
    case None =>
      copy(plannedMoves = ListMap.from(laterMoves)) -> Seq() // no moves to execute, filter out invalid moves
