package route
/**

The route package contains classes related to defining HTTP routes for the application.
 */
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import de.heikoseeberger.akkahttpcirce.FailFastCirceSupport
import io.circe.generic.auto._
import repository.Repository

import scala.concurrent.ExecutionContext

/**
 * The Route class defines the HTTP routes for the application.
 *
 * @param repository The repository for account data.
 * @param ec The implicit execution context.
 */
class Route(repository: Repository)(implicit ec: ExecutionContext) extends FailFastCirceSupport {
  /**
   * Defines the routes for the application.
   */
  def routes =
    (path("hello") & get) {
      complete("ok")
    } ~
      (path("create") & post) {
        val createdAccount = repository.createAccount()
        complete(StatusCodes.Created, createdAccount)
      } ~
      (path("info" / IntNumber) { accountId =>
        complete {
          repository.getAccount(accountId) match {
            case Some(account) => account
            case None => StatusCodes.NotFound -> "Аккаунт не найден"
          }
        }
      })

}

