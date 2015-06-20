package health

object Environment {
  val PREFIX = "HEALTH_"
  val PORT = s"${PREFIX}PORT"
  val ENDPOINTS = s"${PREFIX}ENDPOINTS"

  def getPort : Int = sys.env.getOrElse(PORT, "8080").toInt

  def getEndpoints : Option[String] = sys.env.get(ENDPOINTS)
}
