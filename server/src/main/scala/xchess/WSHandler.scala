package xchess

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.pattern.pipe
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{CompletionStrategy, OverflowStrategy}

import java.util.concurrent.atomic.AtomicInteger
import scala.concurrent.duration.DurationInt
import scala.util.{Success, Try}

class WSHandler(game: ActorRef) extends Actor with ActorLogging {
  import WSHandler._
  val instance: Int = WSHandler.count.incrementAndGet()
  override def preStart(): Unit = log.info(s"$instance: Started for $game")
  override def postStop(): Unit = log.debug(s"$instance: Stopped")

  override def receive: Receive = {
    case sink: ActorRef =>
      context.become(withSink(sink))
      log.debug(s"$instance: Configured sink")
      game ! GameActor.ClientConnected
      context.system.scheduler.scheduleAtFixedRate(15.seconds, 15.seconds, self, ForWS("""{"cmd":"keepalive"}"""))(context.dispatcher)
    case m =>
      log.warning(s"$instance: Sink not configured. Unexpected message: $m")
  }

  def withSink(sink: ActorRef): Receive = {
    case finished: Try[_] =>
      log.info(s"$instance: Finished with $finished")
      game ! GameActor.ClientDisconnected
      context.stop(self)
    case FromWS(message) =>
      log.debug(s"$instance: Received from WS $message")
      game ! GameActor.FromClient(message)
    case ForWS(message) =>
      log.debug(s"$instance: Send to WS $message")
      sink ! message
    case m =>
      log.warning(s"$instance: Unexpected message: $m")
  }
}

object WSHandler extends ClassLogging {
  private case class FromWS(message: String)
  case class ForWS(message: String)

  private val count = new AtomicInteger()

  /** Sets up a WS Message flow connected to a WSHandler Actor instance. */
  def apply(game: ActorRef)(implicit system: ActorSystem): Flow[Message, Message, Any] = {
    import system.dispatcher
    log.debug(s"Initializing websocket message handling.")
    val wsHandler = system.actorOf(Props(new WSHandler(game)))
    val source = Source.actorRef[String](
      { case Done => CompletionStrategy.immediately }: PartialFunction[Any, CompletionStrategy],
      PartialFunction.empty[Any, Throwable],
      8,
      OverflowStrategy.fail
    ).mapMaterializedValue(source => wsHandler ! source).map(TextMessage(_))
    val sink = Sink.foreach[Message] {
      case message: BinaryMessage =>
        message.dataStream.runWith(Sink.ignore) // drain, then ignore
      case message: TextMessage =>
        message.textStream.limitWeighted(2000)(_.length).runWith(Sink.seq)
          .map(s => FromWS(s.mkString)).pipeTo(wsHandler)
    }.mapMaterializedValue(_.transform(Success(_)).pipeTo(wsHandler))
    Flow.fromSinkAndSource[Message, Message](sink, source)
  }
}
