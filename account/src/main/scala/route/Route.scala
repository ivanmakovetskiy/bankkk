package route

import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import scala.concurrent.ExecutionContext

class Route(implicit ec: ExecutionContext) {

  def routes =
    (path("hello") & get) {
      complete("ok")
    }
}
