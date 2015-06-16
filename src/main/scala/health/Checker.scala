package health

import akka.actor.Actor
import spray.routing._
import spray.http._
import tugboat._
import scala.concurrent.ExecutionContext.Implicits.global

class CheckerActor extends Actor with Checker {

  def actorRefFactory = context

  def receive = runRoute(checkRoute)
}

trait Checker extends HttpService {

  val checkRoute =
    path("") {
      get {
        val docker = Docker()
        docker.containers.list().map(_.map(_.id)).foreach(println)
        complete {
          "OK"
        }
      }
    }
}
