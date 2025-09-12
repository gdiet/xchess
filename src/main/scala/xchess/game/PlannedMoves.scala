package xchess.game

import xchess.game.GameTime.*
import xchess.game.Square.*

import scala.collection.immutable.SortedMap

case class PlannedMoves(
                         board: Board,
                         boardPlan: Map[Square, PlanId],
                         timePlan: SortedMap[GameTime, Seq[PlanId]],
                         plans: Map[PlanId, (from: Square, to: Square, time: GameTime)],
                         nextId: PlanId
                       ):

  def plan(from: Square, to: Square, forWhite: Boolean, earliestTime: GameTime): PlannedMoves =
    if !board.size.contains(from, to) then this
    else board.map.get(from) match
      case None => this
      case Some((piece, _)) if piece.isWhite != forWhite => this
      case Some((_, _)) if from == to => removePlannedMove(from)
      case Some((_, frozenUntil)) => addPlannedMove(from, to, max(frozenUntil, earliestTime))
  
  private def removePlannedMove(from: Square): PlannedMoves =
    boardPlan.get(from) match
      case None => this
      case Some(planId) =>
        plans.get(planId) match
          case None => assert(false); this
          case Some((_, _, time)) =>
            copy(
              boardPlan = boardPlan - from,
              timePlan = timePlan.updatedWith(time) {
                case None => assert(false); None
                case some => some.map(_.filter(_ != planId))
              },
              plans = plans - planId
            )

  private def addPlannedMove(from: Square, to: Square, time: GameTime): PlannedMoves =
    copy(
      boardPlan = boardPlan + (from -> nextId),
      timePlan = timePlan.updatedWith(time) {
        case None => Some(Vector(nextId)) // Vector might perform better than Seq here
        case some => some.map(_ :+ nextId)
      },
      plans = plans + (nextId -> (from, to, time)),
      nextId = nextId + 1
    )
