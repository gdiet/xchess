package xchess.game

case class GameOptions(
  boardLayout: String = "standard",
  millisPerTick: Long = 100,
  freezeTicks: Long = 50,
):
  def withLayout(layout: Option[String]): GameOptions =
    copy(boardLayout = layout.getOrElse(boardLayout))

  def withTickMillis(millis: Option[Long]): Either[SetupFailure, GameOptions] =
    if millis.exists(_ < 1) then Left(BadSettings("millisPerTick must be positive"))
    else Right(copy(millisPerTick = millis.getOrElse(millisPerTick)))

  def withFreezeTicks(ticks: Option[Long]): Either[SetupFailure, GameOptions] =
    if ticks.exists(_ < 1) then Left(BadSettings("freezeTicks must be positive"))
    else Right(copy(freezeTicks = ticks.getOrElse(freezeTicks)))
