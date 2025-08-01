package xchess.game

import scala.collection.immutable.ListMap

case class Game(
  board: Board,
  plannedMoves: ListMap[Square, Square],
  freezeTime: Long
)

@annotation.tailrec
def executeAllScheduledMoves(game: Game, gameTime: GameTime): Game =
  executeNextScheduledMove(game, game.plannedMoves.iterator, gameTime) match
    case None => game // no more moves to execute
    case Some(newGame) => executeAllScheduledMoves(newGame, gameTime) // recursively execute next move

@annotation.tailrec
def executeNextScheduledMove(game: Game, moves: Iterator[(Square, Square)], gameTime: GameTime): Option[Game] =
  import game.*
  moves.nextOption match
    case None => None // no more planned moves
    case Some(from, to) => board.map.get(from) match
      case None => Some(game.copy(plannedMoves = plannedMoves - from)) // piece not found, remove move
      case Some(_, frozenUntil) if frozenUntil > gameTime => executeNextScheduledMove(game, moves, gameTime) // piece is frozen, skip this move
      case Some((piece, _)) => Some(handleMoveCommand(game, piece, from, to, gameTime))

def handleMoveCommand(game: Game, piece: Piece, from: Square, to: Square, gameTime: GameTime): Game =
  import game.*
  tryMove(board, piece, from, to) match
    case None => game.copy(plannedMoves = plannedMoves - from) // move not valid, remove it
    case Some(target) =>
      val newBoard = board.copy(map = board.map - from + (target -> (piece, gameTime + freezeTime)))
      game.copy(board = newBoard, plannedMoves = plannedMoves - from - target) // execute the move
