package route

/*
the code defines an Akka HTTP route that matches the "/hello"
path and the GET HTTP method. When a request is made to this route,
 it responds with a complete response containing the string "ok".
 This example showcases a simple HTTP route using Akka HTTP's directives and demonstrates
 how to handle GET requests with a specific path.
 */
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.server.Directives._
import scala.concurrent.ExecutionContext

class Route(implicit ec: ExecutionContext) {

  def routes =
    (path("hello") & get) {
      complete("ok")
    }
}
