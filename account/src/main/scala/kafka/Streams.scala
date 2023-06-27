package kafka

import akka.actor.ActorSystem
import akka.stream.scaladsl.{Flow, Sink, Source}
import repository.Repository
import io.circe.generic.auto._
import model.{AccountUpdate, AccountUpdated}

import scala.concurrent.ExecutionContext

/**
 * Represents a class that processes streams of Kafka messages using Akka Streams.
 *
 * @param repository       The repository used for updating accounts.
 * @param system           The actor system used for Akka operations.
 * @param executionContext The execution context used for asynchronous operations.
 */
class Streams(repository: Repository)(implicit val system: ActorSystem, executionContext: ExecutionContext) extends WithKafka {
  /**
   * Consumes messages of type AccountUpdate from a Kafka topic, updates the repository asynchronously, and produces AccountUpdated messages to a Kafka topic.
   */
  kafkaSource[AccountUpdate]
    .mapAsync(1)(command => repository.update(command.value))
    .map(account => AccountUpdated(account.id, account.amount))
    .to(kafkaSink)
    .run()

  /**
   * Consumes messages of type AccountUpdated from a Kafka topic and prints them to the console.
   */
  kafkaSource[AccountUpdated]
    .map { event =>
      println(s"Получено событие: $event")
      event
    }
    .to(Sink.ignore)
    .run()

}
