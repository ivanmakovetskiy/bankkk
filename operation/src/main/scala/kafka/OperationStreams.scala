package kafka

import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import io.circe.generic.auto._
import commonkafka.WithKafka
import commonmodel.{AccountUpdate, AccountUpdated}
import scala.concurrent.{ExecutionContext, Future}

class OperationStreams()(implicit val system: ActorSystem, executionContext: ExecutionContext)
  extends WithKafka {
  override def group: String = "operation"

  // Consumes messages of type AccountUpdated from Kafka
  kafkaSource[AccountUpdated]
    .mapAsync(1){ e =>
      val destinationId: Int = e.destinationId.getOrElse(0)
      if (destinationId != 0) {
        // Creates an AccountUpdate command based on the received data
        val command: AccountUpdate = AccountUpdate(e.operationId, destinationId, None, -e.value, e.category)
        produceCommand(command)
        // Prints a message indicating the transaction details
        Future(println(s"При переводе №${e.operationId} на счет ${destinationId} была начислена сумма ${-e.value}."))
        // If destinationId is 0, do nothing
      } else{Future(println(" "))}
    }
    .to(Sink.ignore) // Ignores the stream elements
    .run() // Runs the stream
}