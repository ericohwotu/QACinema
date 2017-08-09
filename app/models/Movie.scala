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
