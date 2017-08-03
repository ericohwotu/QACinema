package models

import play.api.libs.json.{Json, OFormat}

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
                 video: Option[String] = None
               )

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
object Movie{implicit val movieFormat :OFormat[Movie] = Json.format[Movie] }
object Booking{implicit val bookings :OFormat[Booking] = Json.format[Booking]}
object trendingMovieList{implicit val movieList :OFormat[trendingMovieList] = Json.format[trendingMovieList]}

object JsonFormats {
  import play.api.libs.json.Json
  implicit val movieFormat = Json.format[trendingMovieList]
}