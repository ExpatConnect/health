package health

import akka.actor.Actor
import spray.routing._
import spray.http._
import spray.client.pipelining._
import scala.concurrent.ExecutionContext.Implicits.global
import scala.concurrent.Future
import scala.util.{Success, Failure}

class CheckerActor extends Actor with Checker {

  def actorRefFactory = context

  def receive = runRoute(checkRoute)
}

trait Checker extends HttpService {

  val healthEndpoints = "ENDPOINTS"
  val healthyStatuses = List(200, 204)
  
  val checkRoute = {
    path("health") {
      get {
        dynamic {
          onComplete(check) {
            case Success(value) => complete("OK")
            case Failure(ex) => failWith(ex)
          }
        }
      }
    }
  }

  def check : Future[List[Unit]] = {
    Future.sequence(endpoints.map(checkIndividual))
  }

  def endpoints : List[String] = {
    sys.env.get(healthEndpoints) match {
      case Some(string) => string.split(",").foldRight(List[String]()) { (endpoint: String, endpoints: List[String]) =>
        "http://" + endpoint :: endpoints
      }
      case None => List()
    }
  }

  def checkIndividual (endpoint : String) : Future[Unit] = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    pipeline(Get(endpoint)).map(endpointOK(endpoint, _))
  }

  def endpointOK (endpoint: String, response: HttpResponse) : Unit = {
    val status = response.status.intValue

    println(s"[healthcheck] Endpoint '$endpoint' returned $status")

    if (!healthyStatuses.contains(status)) {
      throw new Exception(s"Endpoint '$endpoint' is unhealthy")
    }
  }
}
