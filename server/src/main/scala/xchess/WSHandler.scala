package xchess

import akka.Done
import akka.actor.{Actor, ActorLogging, ActorRef, ActorSystem, Props}
import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.pattern.pipe
import akka.stream.scaladsl.{Flow, Sink, Source}
import akka.stream.{CompletionStrategy, OverflowStrategy}

import java.util.concurrent.atomic.AtomicInteger
import scala.util.{Success, Try}

class WSHandler(mainActor: ActorRef) extends Actor with ActorLogging {
  val instance: Int = WSHandler.count.incrementAndGet()
  override def preStart(): Unit = log.info(s"$instance: Started")
  override def postStop(): Unit = log.debug(s"$instance: Stopped")
  override def receive: Receive = {
    case sink: ActorRef => log.debug(s"$instance: Configured sink"); context.become(withSink(sink))
    case m => log.warning(s"$instance: Sink not configured. Unexpected message: $m")
  }
  def withSink(sink: ActorRef): Receive = {
    case finished: Try[Done] =>
      log.info(s"$instance: Finished with $finished")
      context.stop(self)
    case message: String =>
      log.debug(s"$instance: Received $message")
      sink ! message + " response"
    case m =>
      log.warning(s"$instance: Unexpected message: $m")
  }
}

object WSHandler extends ClassLogging {
  private val count = new AtomicInteger()

  /** Sets up a WS Message flow connected to a WSHandler Actor instance. */
  def apply(mainActor: ActorRef)(implicit system: ActorSystem): Flow[Message, Message, Any] = {
    import system.dispatcher
    log.debug(s"Initializing WSFlow.")
    val flowHandler = system.actorOf(Props(new WSHandler(mainActor)))
    val source = Source.actorRef[String](
      { case Done => CompletionStrategy.immediately }: PartialFunction[Any, CompletionStrategy],
      PartialFunction.empty[Any, Throwable],
      8,
      OverflowStrategy.fail
    ).mapMaterializedValue(source => flowHandler ! source).map(TextMessage(_))
    val sink = Sink.foreach[Message] {
      case message: BinaryMessage =>
        message.dataStream.runWith(Sink.ignore) // drain, then ignore
      case message: TextMessage =>
        message.textStream.limitWeighted(2000)(_.length).runWith(Sink.seq).map(_.mkString).pipeTo(flowHandler)
    }.mapMaterializedValue(_.transform(Success(_)).pipeTo(flowHandler))
    Flow.fromSinkAndSource[Message, Message](sink, source)
  }
}
