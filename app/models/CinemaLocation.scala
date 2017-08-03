package models

import play.api.libs.json.{Json, OFormat}

case class CinemaLocation(
                           latitude: String,
                           longitude: String
                         )

object CinemaLocation{ implicit val locations :OFormat[CinemaLocation] = Json.format[CinemaLocation]}