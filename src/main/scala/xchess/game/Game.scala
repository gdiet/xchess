package xchess.game

import xchess.game.Square.string

import java.util.concurrent.TimeUnit.MILLISECONDS
import java.util.concurrent.{ScheduledExecutorService, ScheduledFuture}

// All public methods must be synchronized
class Game(id: String, options: GameOptions, scheduler: ScheduledExecutorService): // id only for logging purposes
  private val clock = GameClock(options.millisPerTick)

  private var subscriptions: Set[Subscription] = Set()
  private var plannedMoves: PlannedMoves = PlannedMoves(Board(options))
  private var chat: List[String] = List()
  private var nextScheduledMove: Option[(time: GameTime, future: ScheduledFuture[?])] = None

  def subscribe(subscription: Subscription): Unit = synchronized {
    println(s"[$id] subscribed: white = ${subscription.isWhite}")
    subscriptions += subscription
    sendInitialMessages(subscription)
  }

  def unsubscribe(subscription: Subscription): Unit = synchronized {
    println(s"[$id] unsubscribed: white = ${subscription.isWhite}")
    subscriptions -= subscription
  }

  def receiveMessage(isWhite: Boolean, message: String): Unit = synchronized {
    println(s"[$id] received message: $message")
    message.split(" ", 2) match
      // FIXME implement other cases

      case Array("start") =>
        if clock.start() then scheduleNextMove()
        broadcast("start")

      case Array("stop") =>
        if clock.stop() then
          nextScheduledMove.foreach(_.future.cancel(false))
          nextScheduledMove = None
        broadcast("stop")

      case Array("chat", message) =>
        chat = message :: chat.take(4)
        broadcast(s"chat $message")

      case Array("plan", move) =>
        move.split(" ") match
          case Array(from, to) =>
            plannedMoves.plan(Square(from), Square(to), isWhite, clock.time + 1) match
              case None => println(s"WARNING - [$id] invalid plan command: $move")
              case Some(newPlannedMoves) =>
                plannedMoves = newPlannedMoves
                broadcast(s"plan $from $to", isWhite)
                if !clock.isStopped then scheduleNextMove()
          case _ => println(s"WARNING - [$id] invalid plan command syntax: $move")

      case _ => println(s"WARNING - [$id] unknown command: $message")
  }

  private def scheduleNextMove(): Unit =
    plannedMoves.timeOfNextPlan.foreach { time =>
      def doSchedule(): Unit =
        nextScheduledMove = Some((time, scheduler.schedule(
          new Runnable { override def run(): Unit = advance() }, clock.millisUntil(time), MILLISECONDS
        )))
      nextScheduledMove match
        case Some((scheduledTime, scheduledFuture)) =>
          if scheduledTime > time then
            scheduledFuture.cancel(false)
            doSchedule()
          else { } /* keep current schedule */
        case None =>
          doSchedule()
    }

  private def advance(): Unit = synchronized {
    val time = clock.time
    println(s"[$id] advancing game at $time")
    val (newPlannedMoves, executedMoves, removedPlans) = plannedMoves.executePlans(time, options.freezeTicks)
    plannedMoves = newPlannedMoves
    removedPlans.foreach(plan =>
      broadcast(s"remove ${plan.from.string}", plan.forWhite)
    )
    executedMoves.foreach(move =>
      broadcast(s"move ${move.from.string} ${move.to.string} ${time.value + options.freezeTicks}")
    )
  }

  private def send(subscription: Subscription, message: String): Unit =
    subscription.send(s"${clock.timeMillis} $message")

  private def broadcast(message: String): Unit =
    val out = s"${clock.timeMillis} $message"
    subscriptions.foreach(_.send(out))

  private def broadcast(message: String, forWhite: Boolean): Unit =
    val out = s"${clock.timeMillis} $message"
    subscriptions.foreach(subscription => if subscription.isWhite == forWhite then subscription.send(out))

  private def sendInitialMessages(subscription: Subscription): Unit =
    send(subscription, s"clock ${clock.time} ${if clock.isStopped then "stopped" else "running"}")
    send(subscription, s"player ${if subscription.isWhite then "white" else "black"}")
    send(subscription, s"boardsize ${plannedMoves.board.size.string}")
    send(subscription, s"millisPerTick ${options.millisPerTick}")
    send(subscription, s"freezeTicks ${options.freezeTicks}")
    plannedMoves.board.map.foreach((square, entry) =>
      send(subscription, s"add ${square.string} ${entry.piece.value} ${entry.frozenUntil}")
    )
    plannedMoves.plannedMoves.values.foreach(plan =>
      if plan.isWhite == subscription.isWhite then send(subscription, s"plan ${plan.from.string} ${plan.to.string}")
    )
    chat.reverse.foreach(message => send(subscription, s"chat $message"))
