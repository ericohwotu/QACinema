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









object Movie{implicit val movieFormat :OFormat[Movie] = Json.format[Movie] }




