package models

import play.api.libs.json.{Json, OFormat}

case class Booking(
                    reference: String,
                    movieName: String,
                    date: String,
                    time: String,
                    seats: List[Seat],
                    price: Double
                  )

object Booking{implicit val bookings :OFormat[Booking] = Json.format[Booking]}