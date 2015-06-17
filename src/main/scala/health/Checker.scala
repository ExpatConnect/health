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
    Future.sequence(hosts.map(checkIndividual))
  }

  def hosts : List[String] = {
    val hosts = sys.env.get("HEALTH")
    hosts match {
      case Some(string) => string.split(",").foldRight(List[String]()) { (host: String, hosts: List[String]) =>
        "http://" + host :: hosts
      }
      case None => List()
    }
  }

  def checkIndividual (host : String) : Future[Unit] = {
    val pipeline: HttpRequest => Future[HttpResponse] = sendReceive
    pipeline(Get(host + "/monitoring/probe")).map(hostOK(host, _))
  }

  def hostOK (host: String, response: HttpResponse) : Unit = {
    val status = response.status.intValue

    println(s"[healthcheck] Host $host returned $status")

    if (status != 200) {
      throw new Exception(s"Host $host is unhealthy")
    }
  }
}
