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

object Movie {
  import play.api.libs.json.{Json, OFormat}
  implicit val movieFormat : OFormat[Movie] = Json.format[Movie]
}