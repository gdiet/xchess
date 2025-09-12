package xchess.game

import xchess.game.GameTime.*
import xchess.game.Square.*
import xchess.util.interleave

import scala.collection.immutable.SortedMap

case class PlannedMoves(
                         board: Board,
                         boardPlan: Map[Square, PlanId],
                         timePlan: SortedMap[GameTime, Seq[PlanId]],
                         plans: Map[PlanId, (isWhite: Boolean, from: Square, to: Square, time: GameTime)],
                         nextId: PlanId
                       ):

  @annotation.tailrec
  final def executePlans(upToTime: GameTime, freezeTime: Long): PlannedMoves =
    timePlan.headOption match
      case None => this
      case Some((time, planIds)) =>
        if time > upToTime then this
        else executePlans(time, planIds, freezeTime).executePlans(upToTime, freezeTime)

  private def executePlans(time: GameTime, planIds: Seq[PlanId], freezeTime: Long): PlannedMoves =
    val whiteFirst = time.whiteFirst
    val (first, second) = planIds.flatMap(plans.get).partition(_.isWhite == whiteFirst)
    val plansToExecute = interleave(first, second)
    val (newBoard, targets) =
      plansToExecute.foldLeft[(Board, Seq[Square])]((board, Seq())) {
        case ((currentBoard, currentTargets), (_, from, to, _)) =>
          currentBoard.executeMove(from, to, time + freezeTime) match
            case None => (currentBoard, currentTargets)
            case Some((newBoard, target)) => (newBoard, currentTargets :+ target)
      }
    val squaresToRemovePlansFrom = targets ++ plansToExecute.map(_.from)
    squaresToRemovePlansFrom.foldLeft(this)(_.removePlannedMove(_)).copy(board = newBoard)

  /** (Un-)plans a move for a player if possible.
    * If from == to, the planned move is removed. */
  def plan(from: Square, to: Square, forWhite: Boolean, earliestTime: GameTime): PlannedMoves =
    if !board.size.contains(from, to) then this
    else board.map.get(from) match
      case None => this
      case Some((piece, frozenUntil)) =>
        if piece.isWhite != forWhite then this
        else if from == to then removePlannedMove(from)
        else addPlannedMove(piece, from, to, max(frozenUntil, earliestTime))

  private def removePlannedMove(from: Square): PlannedMoves =
    boardPlan.get(from) match
      case None => this
      case Some(planId) =>
        plans.get(planId) match
          case None => assert(false); this
          case Some(plan) =>
            copy(
              boardPlan = boardPlan - from,
              timePlan = timePlan.updatedWith(plan.time) {
                case None => assert(false); None
                case some => some.map(_.filter(_ != planId))
              },
              plans = plans - planId
            )

  private def addPlannedMove(piece: Piece, from: Square, to: Square, time: GameTime): PlannedMoves =
    copy(
      boardPlan = boardPlan + (from -> nextId),
      timePlan = timePlan.updatedWith(time) {
        case None => Some(Vector(nextId)) // Vector might perform better than Seq here
        case some => some.map(_ :+ nextId)
      },
      plans = plans + (nextId -> (piece.isWhite, from, to, time)),
      nextId = nextId + 1
    )
