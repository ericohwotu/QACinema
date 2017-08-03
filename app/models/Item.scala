package models

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

case class MovieDB(title: String)

case class CinemaLocations(
                          longitude: String,
                          latitude: String
                          )

object JsonFormats {
  import play.api.libs.json.Json
  implicit val OGmovieFormat = Json.format[Movie]
  implicit val movieFormat = Json.format[MovieDB]
}

