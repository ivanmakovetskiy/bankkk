package repository

import commonkafka.TopicName
import commonmodel.{Account, AccountUpdate}
import kafka.OperationStreams
import model.TransferStart
import io.circe.generic.auto._

/**
 * A repository class that handles transfer operations.
 * @param streams An instance of `OperationStreams` used for producing commands.
 */
class Repository(streams: OperationStreams){
  /**
   * Performs a transfer operation based on the provided `TransferStart` object.
   * If the transfer value is greater than 0, it produces an `AccountUpdate` command and prints a message.
   * @param transfer The `TransferStart` object containing transfer details.
   */
  def transfer(transfer: TransferStart) = {
    if (transfer.value > 0) {
      implicit val commandTopicName: TopicName[AccountUpdate] = streams.simpleTopicName[AccountUpdate]
      streams.produceCommand(AccountUpdate(transfer.transferId, transfer.sourceId, Some(transfer.destinationId), -transfer.value, transfer.category))
      println(s"При переводе №${transfer.transferId} со счета ${transfer.sourceId} была снята сумма ${-transfer.value}.")
    }
  }
}