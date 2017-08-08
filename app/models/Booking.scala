package models

case class Booking(
                    reference: String,
                    movieName: String,
                    date: String,
                    time: String,
                    seats: List[Seat],
                    price: Double
                  )

object Booking {
  import play.api.libs.json._
  implicit val bookingFormat : OFormat[Booking] = Json.format[Booking]
}