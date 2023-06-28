import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import com.typesafe.config.ConfigFactory
import kafka.AccountStreams
import repository.Repository
import route._
/**

The AccountApp object represents the entry point of the application.
 */

object AccountApp extends App  {
  // Create an ActorSystem
  implicit val system: ActorSystem = ActorSystem("App")
  implicit val ec = system.dispatcher

  // Load configuration settings
  private val port = ConfigFactory.load().getInt("port")
  val accountId = ConfigFactory.load().getInt("account.id")
  val defAmount = ConfigFactory.load().getInt("account.amount")

  // Create repository and streams instances
  private val repository = new Repository(accountId, defAmount)
  private val streams = new AccountStreams(repository)

  // Create route instance
  private val route = new Route(repository)

  // Start the HTTP server
  Http().newServerAt("0.0.0.0", port).bind(route.routes)
}