package commonkafka
/*
The commonkafka package contains a module with utility functions
and traits for interacting with Kafka in an Akka-based application.
 */
import akka.actor.ActorSystem
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.stream.scaladsl.{Flow, Source}
import io.circe._
import io.circe.parser._
import io.circe.syntax._
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.reflect.ClassTag

trait WithKafka {
  /**
   * The implicit `ActorSystem` used for interacting with Kafka.
   */
  implicit def system: ActorSystem

  /**
   * The Kafka consumer group to which the application belongs.
   */
  def group: String

  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")
  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)
    .withProperty(ConsumerConfig.GROUP_ID_CONFIG, group)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")
  val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)

  /**
   * Implicit conversion for getting the topic name based on the class name of the type `T`.
   *
   * @param tag The `ClassTag` for the type `T`.
   * @return The `TopicName` for the type `T`.
   */
  implicit def simpleTopicName[T](implicit tag: ClassTag[T]): TopicName[T] = () => tag.runtimeClass.getSimpleName

  /**
   * Creates a source for consuming messages from a Kafka topic and parsing them into objects of type `T`.
   *
   * @param decoder   The `Decoder` for the type `T`.
   * @param topicName The `TopicName` for the type `T`.
   * @tparam T The type of the objects to be consumed.
   * @return A source of objects of type `T` consumed from the Kafka topic.
   */
  def kafkaSource[T](implicit decoder: Decoder[T], topicName: TopicName[T]) = Consumer
    .committableSource(consumerSettings, Subscriptions.topics(topicName.get))
    .map(message => message.record.value())
    .map(body => decode[T](body))
    .collect {
      case Right(command) =>
        command
      case Left(error) => throw new RuntimeException(s"Ошибка при разборе сообщения $error")
    }
    .log(s"Случилась ошибка при чтении топика ${topicName.get}")

  /**
   * Creates a sink for producing objects of type `T` to a Kafka topic.
   *
   * @param encoder    The `Encoder` for the type `T`.
   * @param topicName  The `TopicName` for the type `T`.
   * @tparam T         The type of the objects to be produced.
   * @return A flow that accepts objects of type `T` and produces them to the Kafka topic.
   */
  def kafkaSink[T](implicit encoder: Encoder[T], topicName: TopicName[T]) = Flow[T]
    .map(event => event.asJson.noSpaces)
    .map(value => new ProducerRecord[String, String](topicName.get, value))
    .log(s"Случилась ошибка при обработке сообщения из топика ${topicName.get}")
    .to(Producer.plainSink(producerSettings))

  /**
   * Produces a command of type `T` to the Kafka topic.
   *
   * @param command    The command to be produced.
   * @param encoder    The `Encoder` for the type `T`.
   * @param topicName  The `TopicName` for the type `T`.
   * @tparam T         The type of the command to be produced.
   */
  def produceCommand[T](command: T)(implicit encoder: Encoder[T], topicName: TopicName[T]) = {
    Source
      .single(command)
      .map { command =>
        println(s"Отправляется  сообщение $command в топик ${topicName.get}")
        command.asJson.noSpaces
      }
      .map { value =>
        new ProducerRecord[String, String](topicName.get, value)
      }
      .to(Producer.plainSink(producerSettings))
      .run()
  }
}

/**
* The TopicName trait provides a method for getting the name of a Kafka topic.
*
* @tparam T The type for which the topic name is provided.
/
 */
trait TopicName[T] {
  /**
  * Returns the name of the Kafka topic.
  *
  * @return The name of the Kafka topic.
   */
  def get(): String
}