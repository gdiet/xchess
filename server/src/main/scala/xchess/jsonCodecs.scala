package xchess

import xchess.GameActor.FromClient
import io.circe.parser._
import io.circe.generic.auto._
import xchess.logic.XY

case class PostGameBody(name: String, gameType: String, initialFreeze: Int, freeze: Int)

case class Add(id: Int, x: Int, y: Int, color: String, piece: String, freeze: Long, cmd: String = "add")
case class Move(id: Int, x: Int, y: Int, cmd: String = "move")
case class Remove(id: Int, cmd: String = "remove")
case class ClientPlan(id: Int, color: String, from: XY, to: XY, cmd: String = "plan")
case class Unplan(id: Int, moved: Boolean, cmd: String = "unplan")
case class Winner(winner: String)

case class ClientMove(id: Int, x: Int, y: Int)
object As { def unapply(fromClient: FromClient): Option[ClientMove] = decode[ClientMove](fromClient.message).toOption }
