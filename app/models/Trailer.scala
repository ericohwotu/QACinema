package models
import play.api.libs.json.{Json, OFormat}

case class Trailer(key: String, name: String)
object Trailer{implicit val trailerList :OFormat[Trailer] = Json.format[Trailer]}