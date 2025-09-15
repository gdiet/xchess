package xchess.game

sealed trait SetupFailure:
  def cause: String
  def statusCode: Int

case class BadSettings(cause: String) extends SetupFailure:
  val statusCode = 400

case class Conflict(cause: String) extends SetupFailure:
  val statusCode = 409
