package xchess

import xchess.GameActor.FromClient
import io.circe.parser._
import io.circe.generic.auto._

case class PostGameBody(name: String, gameType: String, initialFreeze: Int, freeze: Int)

case class Add(id: Int, x: Int, y: Int, color: String, piece: String, freeze: Long, cmd: String = "add")
case class Move(id: Int, x: Int, y: Int, cmd: String = "move")
case class Remove(id: Int, cmd: String = "remove")
case class Winner(winner: String)

case class ClientMove(id: Int, x: Int, y: Int)
object As { def unapply(fromClient: FromClient): Option[ClientMove] = decode[ClientMove](fromClient.message).toOption }
