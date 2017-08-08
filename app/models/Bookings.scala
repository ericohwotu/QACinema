package models

import play.api.libs.json.{Json, OFormat}

case class Bookings(
                     seats: List[Seat],
                     price: Double
                   )

object Bookings{implicit val bookings :OFormat[Bookings] = Json.format[Bookings]}