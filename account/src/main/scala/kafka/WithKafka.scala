package kafka

import akka.actor.ActorSystem
import akka.kafka.{ConsumerSettings, ProducerSettings, Subscriptions}
import akka.kafka.scaladsl.{Consumer, Producer}
import akka.stream.scaladsl.{Flow, Source}
import io.circe.{Decoder, Encoder}
import io.circe._
import io.circe.generic.codec.DerivedAsObjectCodec.deriveCodec
import io.circe.parser._
import io.circe.syntax._
import model.AccountUpdate
import org.apache.kafka.clients.consumer.ConsumerConfig
import org.apache.kafka.clients.producer.ProducerRecord
import org.apache.kafka.common.serialization.{StringDeserializer, StringSerializer}

import scala.reflect.ClassTag
/**
 * Provides functionality for interacting with Kafka using Akka.
 */
trait WithKafka {
  /**
   * The implicit `ActorSystem` used for Kafka operations.
   */
  implicit def system: ActorSystem

  /**
   * The Kafka consumer configuration.
   */
  val consumerConfig = system.settings.config.getConfig("akka.kafka.consumer")

  /**
   * The Kafka consumer settings.
   */
  val consumerSettings = ConsumerSettings(consumerConfig, new StringDeserializer, new StringDeserializer)

  /**
   * The Kafka producer configuration.
   */
  val producerConfig = system.settings.config.getConfig("akka.kafka.producer")

  /**
   * The Kafka producer settings.
   */
  val producerSettings = ProducerSettings(producerConfig, new StringSerializer, new StringSerializer)
    .withProperty(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, "earliest")

  /**
   * Defines the Kafka topic based on the class name of the provided type.
   *
   * @param tag The class tag of the type.
   * @return The Kafka topic name.
   */
  def defineTopic[T](implicit tag: ClassTag[T]) = tag.runtimeClass.getSimpleName

  /**
   * Creates a Kafka source for consuming messages of the specified type.
   *
   * @param decoder The decoder for deserializing messages.
   * @param tag     The class tag of the type.
   * @tparam T The type of the consumed messages.
   * @return A source of the consumed messages.
   */
  def kafkaSource[T](implicit decoder: Decoder[T], tag: ClassTag[T]) = Consumer.committableSource(consumerSettings, Subscriptions.topics(defineTopic[T]))
    .map(message => message.record.value())
    .map(body => decode[T](body))
    .collect {
      case Right(command) => command
      case Left(error) => throw new RuntimeException(s"Ошибка при разборе сообщения $error")
    }

  /**
   * Creates a Kafka sink for producing messages of the specified type.
   *
   * @param encoder The encoder for serializing messages.
   * @param tag     The class tag of the type.
   * @tparam T The type of the produced messages.
   * @return A flow that represents the Kafka sink.
   */
  def kafkaSink[T](implicit encoder: Encoder[T], tag: ClassTag[T]) = Flow[T]
    .map(event => event.asJson.noSpaces)
    .map(value => new ProducerRecord[String, String](defineTopic[T], value))
    .log(s"Случилась ошибка при чтении топика ${defineTopic[T]}")
    .to(Producer.plainSink(producerSettings))

  /**
   * Produces a Kafka command message.
   *
   * @param command The command to be produced.
   * @return A `Future` representing the completion of the producing process.
   */
  def produceCommand(command: AccountUpdate) = {
    Source.single(command)
      .map(command => command.asJson.noSpaces)
      .map(value => new ProducerRecord[String, String](defineTopic[AccountUpdate], value))
      .to(Producer.plainSink(producerSettings))
      .run()
  }

}