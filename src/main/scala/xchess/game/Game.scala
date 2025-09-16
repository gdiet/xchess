package xchess.game

import Square.string

// All public methods must be synchronized
class Game(id: String, options: GameOptions): // id only for logging purposes
  private var subscriptions: Set[Subscription] = Set()
  private var plannedMoves: PlannedMoves = PlannedMoves(Board(options))
  private var chat: List[String] = List()
  private val clock = GameClock(options.millisPerTick)

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
        clock.start()
        broadcast("start")

      case Array("stop") =>
        clock.stop()
        broadcast("stop")

      case Array("chat", message) =>
        chat = message :: chat.take(4)
        broadcast(s"chat $message")

      case _ => println(s"WARNING - [$id] unknown command: $message")
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
