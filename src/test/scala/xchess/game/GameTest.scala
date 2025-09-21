package xchess.game

import java.util.concurrent.Executors
import scala.util.chaining.scalaUtilChainingOps

class GameTest extends munit.FunSuite:
  val options = GameOptions(boardLayout = "standard", millisPerTick = 1, freezeTicks = 1)
  var messages: List[String] = List()
  def received: List[String] = messages.reverse.tap(_ => messages = List())

  test("move twice test") {
    val game = Game("test", options, Executors.newScheduledThreadPool(1))
    game.subscribe(new Subscription {
      override def isWhite: Boolean = true
      override def send(message: String): Unit = messages ::= message
      override def close(): Unit = {}
    })
    received
    game.receiveMessage(true, "plan E2 E4")
    assertEquals(received, List("0 plan E2 E4"))
    game.receiveMessage(true, "start")
    assertEquals(received, List("0 start"))
//    Thread.sleep(10)
//    assertEquals(received, List("1 move E2 E4"))
    // FIXME continue
  }
