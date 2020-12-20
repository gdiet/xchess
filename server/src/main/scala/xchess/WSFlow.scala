package xchess

import akka.http.scaladsl.model.ws.{BinaryMessage, Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Sink, Source}

object WSFlow extends ClassLogging {
  def apply()(implicit mat: Materializer): Flow[Message, Message, Any] = {
    log.info(s"Initializing WSFlow.")
    Flow[Message].mapConcat {
      case tm: TextMessage =>
        TextMessage(Source.single("Hello ") ++ tm.textStream ++ Source.single("!")) :: Nil
      case bm: BinaryMessage =>
        bm.dataStream.runWith(Sink.ignore); Nil // drain to avoid the stream being clogged
    }
  }
}
