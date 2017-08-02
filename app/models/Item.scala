package models
case class Movie(
                 id: Int,
                 name: String,
                 certification: String,
                 genre: List[String],
                 synopsis: String,
                 shortDescription: String,
                 image: String,
                 video: String,
                 runTime: Int,
                 directors: List[String],
                 actors: List[String],
                 releaseDate: String
               )

object JsonFormats {
  import play.api.libs.json.Json
  implicit val itemFormat = Json.format[Movie]
}

