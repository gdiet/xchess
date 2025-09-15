package xchess.game

trait Subscription extends AutoCloseable:
  def isWhite: Boolean
  def send(message: String): Unit
