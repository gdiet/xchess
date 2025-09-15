package xchess.game

trait Subscription extends AutoCloseable:
  def send(message: String): Unit
