package xchess.game

import Square.string

class Game(id: String, options: GameOptions): // id only for logging purposes
  private var subscriptions: Set[Subscription] = Set()
  private var plannedMoves: PlannedMoves = PlannedMoves(Board(options))
  private var chat: List[String] = List()

  def subscribe(subscription: Subscription): Unit = synchronized {
    subscriptions += subscription
    sendInitialMessages(subscription)
  }

  def unsubscribe(subscription: Subscription): Unit = synchronized {
    subscriptions -= subscription
  }

  def receiveMessage(isWhite: Boolean, message: String): Unit = synchronized {
    println(s"[$id] received message: $message")
    message.split(" ", 2) match
      // FIXME implement other cases
      case Array("chat", message) =>
        chat = message :: chat.take(4)
        subscriptions.foreach(_.send(s"chat $message"))
      case _ => println(s"WARNING - [$id] unknown command: $message")
  }

  private def sendInitialMessages(subscription: Subscription): Unit =
    subscription.send(s"player ${if subscription.isWhite then "white" else "black"}")
    subscription.send(s"boardsize ${plannedMoves.board.size.string}")
    subscription.send(s"millisPerTick ${options.millisPerTick}")
    subscription.send(s"freezeTicks ${options.freezeTicks}")
    // TODO clock: send current game time and clock status
    plannedMoves.board.map.foreach((square, entry) =>
      subscription.send(s"add ${square.string} ${entry.piece.value} ${entry.frozenUntil}")
    )
    plannedMoves.plannedMoves.values.foreach(plan =>
      if plan.isWhite == subscription.isWhite then subscription.send(s"plan ${plan.from.string} ${plan.to.string}")
    )
    chat.reverse.foreach(message => subscription.send(s"chat $message"))
    subscription.send("connected")
