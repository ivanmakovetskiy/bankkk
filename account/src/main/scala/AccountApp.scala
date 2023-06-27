import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import route.Route


object AccountApp extends App {
  implicit val system: ActorSystem = ActorSystem("App")
  implicit val ec = system.dispatcher

  private val route = new Route()
  val port = ConfigFactory.load().getInt("port")

  Http().newServerAt("0.0.0.0", port).bind(route.routes)
}