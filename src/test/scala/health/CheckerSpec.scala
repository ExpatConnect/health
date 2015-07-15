package health

import org.specs2.mutable._
import spray.testkit.Specs2RouteTest
import spray.http._
import StatusCodes._

import scala.concurrent.Future

class CheckerSpec extends Specification with Specs2RouteTest with Checker {
  sequential

  def actorRefFactory = system

  var rawEndpointsOption: Option[String] = None

  override def rawEndpoints: Option[String] = rawEndpointsOption

  var result: Future[HttpResponse] = Future.successful(HttpResponse(OK))
  var observerdRequest: List[HttpRequest] = List.empty

  override def pipeline = (request: HttpRequest) => {
    observerdRequest = request :: observerdRequest
    result
  }

  "Checker" should {
    "say 200 if no endpoints are configured" in {
      set(None, OK)

      Get("/health") ~> sealRoute(checkRoute) ~> check {
        status === OK
      }
    }

    "say 200 if requests pass" in {
      set(Some("healthy,endpoints"), OK)

      Get("/health") ~> sealRoute(checkRoute) ~> check {
        status === OK
      }
    }

    "httpify the endpoints if not already present" in {
      set(Some("healthy,endpoints"), OK)

      Get("/health") ~> sealRoute(checkRoute) ~> check {
        observerdRequest.filter( !_.uri.toString().startsWith("http://")) should beEmpty
      }
    }

    "not httpify the endpoints if not already present" in {
      set(Some("http://healthy,http://endpoints"), OK)

      Get("/health") ~> sealRoute(checkRoute) ~> check {
        observerdRequest.filter( _.uri.toString().startsWith("http://http")) should beEmpty
      }
    }


    "say 500 if a request fails" in {
      set(Some("http://dev.null"), InternalServerError)

      Get("/health") ~> sealRoute(checkRoute) ~> check {
        status === InternalServerError
      }
    }
  }

  def set(rawEndpoints: Option[String], status: StatusCode) = {
    rawEndpointsOption = rawEndpoints
    result = Future.successful(HttpResponse(status))
  }
}