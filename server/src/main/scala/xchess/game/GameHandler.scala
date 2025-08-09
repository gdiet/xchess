package xchess.game

import xchess.game.GameHandler.Subscription
import xchess.game.Square.*

import java.util.concurrent.Executors

object GameHandler:
  trait Subscription extends AutoCloseable {
    def message(message: String): Unit
  }

class GameHandler {
  private def numberOfCores = Runtime.getRuntime.availableProcessors()
  private val scheduler = Executors.newScheduledThreadPool(numberOfCores)
  private val eventTimer = EventTimer(scheduler, 3000, () => println("event timer tick")) // FIXME
  private var chat: List[String] = List()
  private var subscriptions: Set[Subscription] = Set()
  private var game: Game = Game("standard")

  def subscribe(subscription: Subscription): Unit = synchronized {
    subscriptions += subscription
    subscription.message(s"freeze: ${game.freezeTime}")
    subscription.message(s"clock: ${eventTimer.time} ${if eventTimer.isStopped then "stopped" else "running"}")
    subscription.message(s"cols: ${game.board.size.col.value + 1}")
    subscription.message(s"rows: ${game.board.size.row.value + 1}")
    game.board.map.foreach((square, entry) => {
      subscription.message(s"board: ${square.string} ${entry.piece.value} ${entry.frozenUntil}")
    })
    chat.reverse.foreach(message => subscription.message(s"chat: $message"))
  }

  def unsubscribe(subscription: Subscription): Unit = synchronized {
    subscriptions -= subscription
  }

  def receiveMessage(message: String): Unit = synchronized {
    message.split(":", 2) match {
      case Array("stop") => eventTimer.stop(); println(s"clock stopped at ${eventTimer.time}")
      case Array("start") => eventTimer.start(); println(s"clock started at ${eventTimer.time}")
      case Array("move", move) => println(s"move: $move")
      case Array("chat", message) =>
        chat = message :: chat.take(4)
        subscriptions.foreach(_.message(s"chat:$message"))
      case _ => println(s"WARNING - unknown command: $message")
    }
  }
}
