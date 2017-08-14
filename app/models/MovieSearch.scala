package models

import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.{JsObject, Json}

case class MovieSearch(title : Option[String], genres : Option[String], actors: Option[String], director: Option[String], plot : Option[String]) {
  def jsonObject : JsObject = Json.obj(
    "Title" -> Json.obj("$regex" -> title.getOrElse(".").toString),
    "Genre" -> Json.obj("$regex" -> genres.getOrElse(".").toString),
    "Actors" -> Json.obj("$regex" -> actors.getOrElse(".").toString),
    "Director" -> Json.obj("$regex" -> director.getOrElse(".").toString),
    "Plot" -> Json.obj("$regex" -> plot.getOrElse(".").toString)
  )
}

object MovieSearch {
  val movieSearchForm: Form[MovieSearch] = Form(
    mapping(
      "Title" -> optional(text),
      "Genres" -> optional(text),
      "Director" -> optional(text),
      "Actors" -> optional(text),
      "Plot" -> optional(text)
    )(MovieSearch.apply)(MovieSearch.unapply)
  )
}