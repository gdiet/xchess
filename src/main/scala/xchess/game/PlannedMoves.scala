package xchess.game

import xchess.game.GameTime.*
import xchess.game.Square.*
import xchess.util.interleave

import scala.collection.immutable.SortedMap

case class PlannedMoves(
                         board: Board,
                         private val plans: Map[PlanId, (isWhite: Boolean, from: Square, to: Square, time: GameTime)] = Map(),
                         private val boardPlan: Map[Square, PlanId] = Map(),
                         private val timePlan: SortedMap[GameTime, Seq[PlanId]] = SortedMap(),
                         private val nextId: PlanId = PlanId.zero
                       ):

  def timeOfNextPlan: Option[GameTime] = timePlan.keysIterator.nextOption
  def plannedMoves: Map[PlanId, (isWhite: Boolean, from: Square, to: Square, time: GameTime)] = plans

  private type Move = (from: Square, to: Square)
  private type PlanReference = (from: Square, forWhite: Boolean)
  
  /** @return planned moves, executed moves, removed plans. */
  final def executePlans(upToTime: GameTime, freezeTicks: Long): (PlannedMoves, Seq[Move], Seq[PlanReference] ) =
    timePlan
      .takeWhile { case (time, _) => time <= upToTime }
      .foldLeft((this, Seq[Move](), Seq[PlanReference]())) {
        case ((currentPlannedMoves, executedMoves, removedPlans), (time, planIds)) =>
          val (nextPlannedMoves, executed, removed) = executePlans(time, planIds, freezeTicks)
          (nextPlannedMoves, executedMoves ++ executed, removedPlans ++ removed)
      }

  private def executePlans(time: GameTime, planIds: Seq[PlanId], freezeTicks: Long):
        (PlannedMoves, Seq[Move], Seq[PlanReference]) =
    val whiteFirst = time.whiteFirst
    val plansToExecute = planIds.flatMap(plans.get)
    val (first, second) = plansToExecute.partition(_.isWhite == whiteFirst)
    val (newBoard, moves) =
      interleave(first, second).foldLeft((board, Seq[Move]())) {
        case ((currentBoard, currentMoves), (_, from, to, _)) =>
          if (currentMoves.exists(_.to == from)) (currentBoard, currentMoves) // piece was captured
          else currentBoard.executeMove(from, to, time + freezeTicks) match
            case None => (currentBoard, currentMoves)
            case Some((newBoard, target)) => (newBoard, currentMoves :+ (from, target))
      }
    val squaresToRemovePlansFrom = (plansToExecute.map(_.from) ++ moves.map(_.to)).distinct
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
  def plan(from: Square, to: Square, forWhite: Boolean, earliestTime: GameTime): Option[PlannedMoves] =
    assert(from != to)
    if !board.size.contains(to) then None
    else board.map.get(from).flatMap { case (piece, frozenUntil) =>
      if piece.isWhite != forWhite then None else
        val time = max(frozenUntil, earliestTime)
        Some(addPlannedMove(piece, from, to, time))
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
