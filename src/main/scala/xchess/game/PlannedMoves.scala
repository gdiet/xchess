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

  /** @return planned moves, executed moves, removed plans. */
  final def executePlans2(upToTime: GameTime, freezeTime: Long): // TODO use private types for readability
    (PlannedMoves, Seq[(from: Square, to: Square)], Seq[(from: Square, forWhite: Boolean)] ) =
    timePlan.takeWhile(_._1 <= upToTime).foldLeft(
      (this, Seq[(from: Square, to: Square)](), Seq[(from: Square, forWhite: Boolean)]())
    ) { case ((currentPlannedMoves, executedMoves, removedPlans), (time, planIds)) =>
      val (nextPlannedMoves, executed, removed) = executePlans2(time, planIds, freezeTime)
      (nextPlannedMoves, executedMoves ++ executed, removedPlans ++ removed)
    }

  private def executePlans2(time: GameTime, planIds: Seq[PlanId], freezeTime: Long):
    (PlannedMoves, Seq[(from: Square, to: Square)], Seq[(from: Square, forWhite: Boolean)]) = {



    ???
  }

  /** FIXME needs to return the list of executed moves and the list of removed plans. */
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
      plansToExecute.foldLeft((board, Seq[Square]())) {
        case ((currentBoard, currentTargets), (_, from, to, _)) =>
          currentBoard.executeMove(from, to, time + freezeTime) match
            case None => (currentBoard, currentTargets)
            case Some((newBoard, target)) => (newBoard, currentTargets :+ target)
      }
    val squaresToRemovePlansFrom = targets ++ plansToExecute.map(_.from)
    // TODO should probably be inlined
    squaresToRemovePlansFrom.flatMap(boardPlan.get) // TODO and so on
    squaresToRemovePlansFrom.foldLeft(this)(_.removePlannedMove(_)).copy(board = newBoard)

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

  /** Plans a move for a player if possible.
    *
    * @return planned time + planned moves, or None for invalid plan. */
  def plan(from: Square, to: Square, forWhite: Boolean, earliestTime: GameTime): Option[(GameTime, PlannedMoves)] =
    assert(from != to)
    if !board.size.contains(to) then None
    else board.map.get(from).flatMap { case (piece, frozenUntil) =>
      if piece.isWhite != forWhite then None else
        val time = max(frozenUntil, earliestTime)
        Some((time, addPlannedMove(piece, from, to, time)))
    }

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

  /** Remove a move plan for a player if possible.
    *
    * @return planned moves, or None if no plan is found. */
  def unPlan(from: Square, forWhite: Boolean): Option[PlannedMoves] =
    boardPlan.get(from).flatMap(planId =>
      plans.get(planId) match
        case None => assert(false); None
        case Some(plan) =>
          if plan.isWhite != forWhite then None
          else Some(copy(
            boardPlan = boardPlan - from,
            timePlan = timePlan.updatedWith(plan.time) {
              case None => assert(false); None
              case some => some.map(_.filter(_ != planId))
            },
            plans = plans - planId
          ))
    )
