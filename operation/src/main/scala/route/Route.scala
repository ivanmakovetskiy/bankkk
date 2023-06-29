package route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import io.circe.generic.auto._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import commonkafka.TopicName
import commonmodel.AccountUpdate
import kafka.OperationStreams
import model.TransferStart
import repository.Repository

import scala.concurrent.ExecutionContext


class Route(streams: OperationStreams, repository: Repository)(implicit ec: ExecutionContext) extends FailFastCirceSupport {

  implicit val commandTopicName: TopicName[AccountUpdate] = streams.simpleTopicName[AccountUpdate]

  def routes =
    (path("hello") & get) {
      complete("ok")
    } ~
      (path("update" / IntNumber / IntNumber / Segment / Segment) { (operationId, accountId, valueStr, category) =>
        val value = valueStr.toInt
        val command = AccountUpdate(operationId, accountId, None, value, Some(category))
        streams.produceCommand(command)
        complete(command)
      }) ~
      (path("transfer") & post & entity(as[TransferStart])) { transfer =>
        repository.transfer(transfer)
        complete(transfer)
      }
}

