package models

import play.api.data._
import play.api.data.Forms._
import play.api.libs.json.{JsObject, Json}

case class MovieSearch(title : Option[String], genres : Option[String], actors: Option[String], director: Option[String], plot : Option[String]) {
  def jsonObject : JsObject = Json.obj(
    "Title" -> Json.obj("$regex" -> title.getOrElse(".").toString, "$options" -> "i"),
    "Genre" -> Json.obj("$regex" -> genres.getOrElse(".").toString, "$options" -> "i"),
    "Actors" -> Json.obj("$regex" -> actors.getOrElse(".").toString, "$options" -> "i"),
    "Director" -> Json.obj("$regex" -> director.getOrElse(".").toString, "$options" -> "i"),
    "Plot" -> Json.obj("$regex" -> plot.getOrElse(".").toString, "$options" -> "i")
  )

  override def toString: String = {
    val seq : Seq[Option[(String, String)]] =
      Seq(
        title.map(x => ("Title", x)),
        genres.map(x => ("Genre", x)),
        actors.map(x => ("Actor", x)),
        director.map(x => ("Director", x)),
        plot.map(x => ("Plot", x))
      )

    seq.map {
      case Some((key, value)) => key +  ": " + value + " "
      case _ => ""
    }.mkString
  }
}

object MovieSearch {
  def validation(vlu : Option[String]) = vlu.getOrElse("") match {
    case alphanumericRegex(_*) => true
    case _ => false
  }

  val alphanumericRegex = """[A-Za-z]*""".r
  val message = "Invalid characters used in search query."

  val movieSearchForm: Form[MovieSearch] = Form(
    mapping(
      "Title" -> optional(text).verifying(message, vlu => validation(vlu)),
      "Genre" -> optional(text).verifying(message, vlu => validation(vlu)),
      "Actors" -> optional(text).verifying(message, vlu => validation(vlu)),
      "Director" -> optional(text).verifying(message, vlu => validation(vlu)),
      "Plot" -> optional(text).verifying(message, vlu => validation(vlu))
    )(MovieSearch.apply)(MovieSearch.unapply)
  )
}