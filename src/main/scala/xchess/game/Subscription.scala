package xchess.game

trait Subscription extends AutoCloseable:
  def onNext(message: String): Unit
