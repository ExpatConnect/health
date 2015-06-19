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

  val HEALTH_ENDPOINTS = "ENDPOINTS"

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
    rawEndpoints match {
      case Some(string) => string.split(",").foldRight(List[String]()) { (endpoint: String, endpoints: List[String]) =>
        if (endpoint.startsWith("http://")) {
          endpoint :: endpoints
        } else {
          "http://" + endpoint :: endpoints
        }
      }
      case None => List()
    }
  }

  def rawEndpoints: Option[String] = {
    sys.env.get(HEALTH_ENDPOINTS)
  }

  def checkIndividual (endpoint : String) : Future[Unit] = {
    pipeline(Get(endpoint)).map(endpointOK(endpoint, _))
  }

  def pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  def endpointOK (endpoint: String, response: HttpResponse) : Unit = {
    println(s"[healthcheck] Endpoint '$endpoint' returned ${response.status}")

    if (response.status.isFailure) {
      throw new Exception(s"Endpoint '$endpoint' is unhealthy")
    }
  }
}
