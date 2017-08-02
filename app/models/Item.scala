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

case class MovieDB(
                  title: String,
                  backdrop_path: String,
                  overview: String,
                  poster_path: String,
                  release_date: String,
                  genre_ids: List[Int],
                  vote_average: Double
                  )

case class MovieDB2(
                    title: String,
                    backdrop_path: String,
                    overview: String,
                    poster_path: String,
                    release_date: String,
                    genre_ids: List[String],
                    vote_average: Double
                  )

case class genreConverter(
                         id: Int,
                         name: String
                         )

object JsonFormats {
  import play.api.libs.json.Json
  implicit val movieFormat = Json.format[MovieDB]
  implicit val movieFormat2 = Json.format[MovieDB2]
  implicit val genreFormat = Json.format[genreConverter]

}

