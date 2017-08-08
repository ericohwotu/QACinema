package models

case class CinemaLocation(latitude: String, longitude: String)

object CinemaLocation {
  import play.api.libs.json._
  implicit val cinemaLocationFormat : OFormat[CinemaLocation] = Json.format[CinemaLocation]
}