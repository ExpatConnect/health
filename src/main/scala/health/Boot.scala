package health

import akka.actor.{ActorSystem, Props}
import akka.io.IO
import spray.can.Http
import akka.pattern.ask
import akka.util.Timeout

object Boot extends App {
  implicit val system = ActorSystem("healt-check")

  val service = system.actorOf(Props[CheckerActor], "checker-service")

  implicit val timeout = Timeout(Environment.timeout)

  IO(Http) ? Http.Bind(service, interface = "0.0.0.0", port = Environment.port)
}

