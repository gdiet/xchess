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
  private var nextScheduledMove: Option[(time: GameTime, future: ScheduledFuture[Unit])] = None

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
        if clock.start() then
          plannedMoves.timeOfNextPlan.foreach { time => scheduler.schedule(
            new Runnable { override def run(): Unit = advance() },
            clock.millisUntil(time), MILLISECONDS
          ) }
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
              case Some((newPlannedMoves, plannedTime)) =>
                plannedMoves = newPlannedMoves
                broadcast(s"plan $from $to ${clock.millisUntil(plannedTime)}", isWhite)
          case _ => println(s"WARNING - [$id] invalid plan command syntax: $move")

      case _ => println(s"WARNING - [$id] unknown command: $message")
  }

  private def advance(): Unit = synchronized {
    println(s"[$id] advancing game at ${clock.time}")
    // FIXME continue
//    val time = clock.time
//    val (newPlannedMoves, moves) = plannedMoves.executeScheduledMoves(time)
//    plannedMoves = newPlannedMoves
//    moves.foreach((from, to) => broadcast(s"move ${from.string} ${to.string}"))
//    broadcast(s"advance $time")
//    nextScheduledMove = None
//    plannedMoves.timeOfNextPlan.foreach { nextTime =>
//      val delay = clock.millisUntil(nextTime)
//      val future = scheduler.schedule(() => advance(), delay, java.util.concurrent.TimeUnit.MILLISECONDS)
//      nextScheduledMove = Some((nextTime, future))
//    }
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
