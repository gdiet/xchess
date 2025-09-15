package xchess.game

class Game(id: String, options: GameOptions): // id only for logging purposes
  private var subscriptions: Set[Subscription] = Set()
  private var plannedMoves: PlannedMoves = PlannedMoves(Board(options))

  def subscribe(subscription: Subscription): Unit = synchronized {
    subscriptions += subscription
  }

  def unsubscribe(subscription: Subscription): Unit = synchronized {
    subscriptions -= subscription
  }

  def receiveMessage(message: String): Unit = synchronized {
    ???
  }

  private def sendInitialMessages(subscription: Subscription): Unit = synchronized {
    // boardsize
    // millisPerTick
    // freezeTicks
    // clock
    // add
    // plan
    // chat
    // connected
  }
