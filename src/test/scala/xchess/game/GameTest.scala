package xchess.game

import java.util.concurrent.Executors
import scala.util.chaining.scalaUtilChainingOps

class GameTest extends munit.FunSuite:
  val options = GameOptions(boardLayout = "standard", millisPerTick = 1, freezeTicks = 1)
  val scheduler = Executors.newScheduledThreadPool(1)
  var messages: List[String] = List()
  def received: List[String] = messages.reverse.tap(_ => messages = List())

  test("move twice test") {
    val game = Game("test", options, scheduler)
    game.subscribe(new Subscription {
      override def isWhite: Boolean = true
      override def send(message: String): Unit = messages ::= message.split(" ", 2)(1)
      override def close(): Unit = {}
    })
    received
    game.receiveMessage(true, "plan E2 E4")
    assertEquals(received, List("plan E2 E4"))
    game.receiveMessage(true, "start")
    assertEquals(received, List("start"))
    Thread.sleep(100) // TODO use scheduler to wait instead
    val List(unplan, move) = received
    assertEquals(unplan, "unplan E2")
    assertEquals(move.split(" ").dropRight(1).mkString(" "), "move E2 E4")
    game.receiveMessage(true, "plan E4 E5")
    assertEquals(received, List("plan E4 E5"))
    Thread.sleep(100) // TODO use scheduler to wait instead
    println(received)
    // FIXME continue
  }
