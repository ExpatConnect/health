package health

import java.util

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

  val checkRoute = {
    path("health") {
      get {
        dynamic {
          onComplete(check) {
            case Success(value) => {
              value.foreach(println)
              complete("OK")
            }
            case Failure(ex) => failWith(ex)
          }
        }
      }
    }
  }

  def check: Future[List[String]] = {
    Future.sequence(endpoints.map(checkIndividual))
  }

  def httpIfy(endpoint: String): String =
    if (endpoint.startsWith("http://")) {
      endpoint
    } else {
      "http://" + endpoint
    }


  def endpoints: List[String] = rawEndpoints
    .map(_.split(","))
    .fold(List.empty[String])(_.to)
    .map(httpIfy)

  def rawEndpoints: Option[String] = {
    Environment.endpoints
  }

  def checkIndividual(endpoint: String): Future[String] = {
    pipeline(Get(endpoint)).map(endpointOK(endpoint, _))
  }

  def pipeline: HttpRequest => Future[HttpResponse] = sendReceive

  def endpointOK(endpoint: String, response: HttpResponse): String = {
    if (response.status.isFailure) {
      throw new Exception(s"Endpoint '$endpoint' is unhealthy and returned ${response.status}")
    } else {
      s"[healthcheck] Endpoint '$endpoint' returned ${response.status}"
    }
  }
}
