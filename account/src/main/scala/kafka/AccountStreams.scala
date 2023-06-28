package kafka
/**
* The kafka package contains classes related to Kafka integration in a Scala application.
 **/
import akka.actor.ActorSystem
import akka.stream.scaladsl.Sink
import io.circe.generic.auto._
import commonkafka.WithKafka
import model.{AccountUpdate, AccountUpdated}
import repository.Repository

import scala.concurrent.ExecutionContext

/**
 * The AccountStreams class provides Kafka stream processing for account-related events.
 *
 * @param repository The repository for account data.
 * @param system The implicit ActorSystem.
 * @param executionContext The implicit execution context.
 */
class AccountStreams(repository: Repository)(implicit val system: ActorSystem, executionContext: ExecutionContext)
  extends WithKafka {
  /**
   * The consumer group name for the account streams.
   */
  def group = s"account-${repository.accountId}"

  // Kafka stream for consuming AccountUpdate commands
  kafkaSource[AccountUpdate]
    .filter(command => repository.getAccount(command.accountId).exists(_.amount + command.value >= 0))
    .mapAsync(1) { command =>
      repository
        .update(command.accountId, command.value)
        .map(_ => AccountUpdated(
          operationId = command.operationId,
          accountId = command.accountId,
          destinationId = command.destinationId,
          value = command.value,
          category = command.category))
    }
    .to(kafkaSink)
    .run()

  // Kafka stream for consuming AccountUpdated events
  kafkaSource[AccountUpdated]
    .filter(event => repository.getAccount(event.accountId).nonEmpty)
    .map { e =>
      println(s"Счет ${e.accountId} обновлен на сумму ${e.value}.")
      e
    }
    .to(Sink.ignore)
    .run()
}