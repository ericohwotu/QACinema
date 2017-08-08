package models

case class TrendingMovie(title: String)

object TrendingMovie {
  import play.api.libs.json._
  implicit val trendingMovieFormat : OFormat[TrendingMovie] = Json.format[TrendingMovie]
}