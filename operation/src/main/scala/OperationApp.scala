import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import kafka.OperationStreams
import repository.Repository
import route.Route

object OperationApp extends App  {
  // Initialize the actor system
  implicit val system: ActorSystem = ActorSystem("OperationApp")

  // Set the execution context
  implicit val ec = system.dispatcher

  // Load the port from the configuration file
  val port = ConfigFactory.load().getInt("port")

  // Create the operation streams
  private val streams = new OperationStreams()

  // Create the repository
  private val repository = new Repository(streams)
  // Create the route
  private val route = new Route(streams, repository)

  // Start the HTTP server
  Http().newServerAt("0.0.0.0", port).bind(route.routes)
}