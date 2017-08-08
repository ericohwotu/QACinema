package models

import play.api.libs.json.{Json, OFormat}
import play.api.data._
import play.api.data.Forms._
import scala.collection.mutable.ArrayBuffer
import play.api.libs.json.Json


case class Movie(
                 Title: String,
                 Rated: String,
                 Released: String,
                 Runtime: String,
                 Genre: String,
                 Director: String,
                 Actors: String,
                 Plot: String,
                 Poster: String,
                 var video: Option[String] = None
               )

object Movie{
  implicit val movieFormat :OFormat[Movie] = Json.format[Movie]

  val createMovieForm = Form(
    mapping(
      "Title" -> nonEmptyText,
      "Rated" -> nonEmptyText,
      "Released" -> nonEmptyText,
      "Runtime" -> nonEmptyText,
      "Genre" -> nonEmptyText,
      "Director" -> nonEmptyText,
      "Actors" -> nonEmptyText,
      "Plot" -> nonEmptyText,
      "Poster" -> nonEmptyText,
      "video" -> optional(text)
    )(Movie.apply)(Movie.unapply)
  )
}
case class trendingMovieList(title: String)

case class Booking(
                    reference: String,
                    movieName: String,
                    date: String,
                    time: String,
                   seats: List[Seat],
                   price: Double
                   )

case class CinemaLocation(
                         latitude: String,
                         longitude: String
                         )

object CinemaLocation{ implicit val locations :OFormat[CinemaLocation] = Json.format[CinemaLocation]}
object Booking{implicit val bookings :OFormat[Booking] = Json.format[Booking]}
object trendingMovieList{implicit val movieList :OFormat[trendingMovieList] = Json.format[trendingMovieList]}

object JsonFormats {
  import play.api.libs.json.Json
  implicit val movieFormat = Json.format[trendingMovieList]
}