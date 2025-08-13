package xchess.game

import xchess.game.GameHandler.Subscription
import xchess.game.Square.*

import java.util.concurrent.Executors

object GameHandler:
  trait Subscription extends AutoCloseable {
    def message(message: String): Unit
  }

class GameHandler(id: String):
  private def numberOfCores = Runtime.getRuntime.availableProcessors()
  private val scheduler = Executors.newScheduledThreadPool(numberOfCores)
  private val eventTimer = EventTimer(scheduler, 3000, () => advance())
  private var chat: List[String] = List()
  private var subscriptions: Set[Subscription] = Set()
  private var game: Game = Game("standard")

  private def advance(): Unit = synchronized {
    val time = eventTimer.time
    val (newGame, moves) = executeScheduledMoves(game, time)
    game = newGame
    moves.foreach((from, to) => subscriptions.foreach(_.message(s"move: ${from.string} ${to.string}")))
    subscriptions.foreach(_.message(s"advance: $time"))
  }

  def subscribe(subscription: Subscription): Unit = synchronized {
    subscriptions += subscription
    subscription.message(s"freeze: ${game.freezeTime}")
    subscription.message(s"clock: ${eventTimer.time} ${if eventTimer.isStopped then "stopped" else "running"}")
    subscription.message(s"cols: ${game.board.size.col.value + 1}")
    subscription.message(s"rows: ${game.board.size.row.value + 1}")
    game.board.map.foreach((square, entry) => {
      subscription.message(s"board: ${square.string} ${entry.piece.value} ${entry.frozenUntil}")
    })
    game.plannedMoves.foreach((from, to) =>
      subscription.message(s"plan: ${from.string} ${to.string}")
    )
    chat.reverse.foreach(message => subscription.message(s"chat: $message"))
  }

  def unsubscribe(subscription: Subscription): Unit = synchronized {
    subscriptions -= subscription
  }

  def receiveMessage(message: String): Unit = synchronized {
    message.split(": ", 2) match {
      case Array("stop") => eventTimer.stop()
      case Array("start") => eventTimer.start()
      case Array("plan", move) =>
        move.split(" ").map(Square.apply) match
          case Array(from, to) if from != to && game.board.map.contains(from) =>
            game = game.copy(plannedMoves = game.plannedMoves + (from -> to))
            subscriptions.foreach(_.message(s"plan: ${from.string} ${to.string}"))
          case _ => println(s"WARNING - [$id] invalid plan command: $move")
      case Array("chat", message) =>
        chat = message :: chat.take(4)
        subscriptions.foreach(_.message(s"chat:$message"))
      case _ => println(s"WARNING - [$id] unknown command: $message")
    }
  }
