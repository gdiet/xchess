package xchess.game

import Square.string

class Game(id: String, options: GameOptions): // id only for logging purposes
  private var subscriptions: Set[Subscription] = Set()
  private var plannedMoves: PlannedMoves = PlannedMoves(Board(options))

  def subscribe(subscription: Subscription): Unit = synchronized {
    subscriptions += subscription
    sendInitialMessages(subscription)
  }

  def unsubscribe(subscription: Subscription): Unit = synchronized {
    subscriptions -= subscription
  }

  def receiveMessage(message: String): Unit = synchronized {
    println(s"[$id] received message: $message")
    // FIXME implement
  }

  private def sendInitialMessages(subscription: Subscription): Unit =
    subscription.send(s"boardsize ${plannedMoves.board.size.string}")
    subscription.send(s"millisPerTick ${options.millisPerTick}")
    subscription.send(s"freezeTicks ${options.freezeTicks}")
    // TODO clock: send current game time and clock status
    plannedMoves.board.map.foreach((square, entry) =>
      subscription.send(s"add ${square.string} ${entry.piece.value} ${entry.frozenUntil}")
    )
    // TODO when sending plans, we need to know whether the player is white or black
    // plan
    // chat
    // connected (as status message)
