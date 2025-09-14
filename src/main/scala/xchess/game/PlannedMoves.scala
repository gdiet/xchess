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

  private type Move = (from: Square, to: Square)
  private type PlanReference = (from: Square, forWhite: Boolean)

  /** @return planned moves, executed moves, removed plans. */
  final def executePlans(upToTime: GameTime, freezeTime: Long): (PlannedMoves, Seq[Move], Seq[PlanReference] ) =
    timePlan
      .takeWhile { case (time, _) => time <= upToTime }
      .foldLeft((this, Seq[Move](), Seq[PlanReference]())) {
        case ((currentPlannedMoves, executedMoves, removedPlans), (time, planIds)) =>
          val (nextPlannedMoves, executed, removed) = executePlans(time, planIds, freezeTime)
          (nextPlannedMoves, executedMoves ++ executed, removedPlans ++ removed)
      }

  private def executePlans(time: GameTime, planIds: Seq[PlanId], freezeTime: Long):
        (PlannedMoves, Seq[Move], Seq[PlanReference]) =
    val whiteFirst = time.whiteFirst
    val (first, second) = planIds.flatMap(plans.get).partition(_.isWhite == whiteFirst)
    val plansToExecute = interleave(first, second)
    val (newBoard, moves) =
      plansToExecute.foldLeft((board, Seq[Move]())) {
        case ((currentBoard, currentMoves), (_, from, to, _)) =>
          currentBoard.executeMove(from, to, time + freezeTime) match
            case None => (currentBoard, currentMoves)
            case Some((newBoard, target)) => (newBoard, currentMoves :+ (from, target))
      }
    val squaresToRemovePlansFrom = moves.map(_.to) ++ plansToExecute.map(_.from)
    val plansToRemove = squaresToRemovePlansFrom.flatMap(boardPlan.get)
    val removedPlansRefs = plansToRemove.flatMap { plans.get(_).map(plan => (plan.from, plan.isWhite)) }
    (copy(
      board = newBoard,
      boardPlan = boardPlan -- squaresToRemovePlansFrom,
      timePlan = timePlan - time,
      plans = plans -- plansToRemove
    ), moves, removedPlansRefs)

  /** Plans a move for a player if possible.
    *
    * @return planned moves + planned time for the move, or None for invalid plan. */
  def plan(from: Square, to: Square, forWhite: Boolean, earliestTime: GameTime): Option[(PlannedMoves, GameTime)] =
    assert(from != to)
    if !board.size.contains(to) then None
    else board.map.get(from).flatMap { case (piece, frozenUntil) =>
      if piece.isWhite != forWhite then None else
        val time = max(frozenUntil, earliestTime)
        Some((addPlannedMove(piece, from, to, time), time))
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
