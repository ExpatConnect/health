package health

import scala.concurrent.duration._

object Environment {

  val PREFIX = "HEALTH_"
  val PORT = s"${PREFIX}PORT"
  val TIMEOUT = s"${PREFIX}TIMEOUT"
  val ENDPOINTS = s"${PREFIX}ENDPOINTS"

  val port = getInt(PORT, 8080)
  val endpoints = sys.env.get(ENDPOINTS)
  val timeout = getInt(TIMEOUT, 5).second

  private def getInt(key: String, default : Int) : Int = {
    sys.env.get(key) match {
      case Some(string) => string.toInt
      case None => default
    }
  }
}
