package controllers

import javax.inject.Inject

import scala.concurrent.Future
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import reactivemongo.api.Cursor
import models._
import models.JsonFormats._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._
import collection._
import reactivemongo.bson.BSONDocument
import scalaj.http._

class Mongo @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents{

  def movieDBTable: Future[JSONCollection] = database.map(_.collection[JSONCollection]("movieDB"))


  def createAPI(): Action[AnyContent] = Action {
    getTrending.foreach { movie =>
      val improvedFormat = movie.title.replaceAll("\\s", "+")
      val trendingNamesReq = Http("http://www.omdbapi.com/?t=" + improvedFormat + "&r=json&plot=full&apikey=313dd87a")
      val newMovie = Json.parse(trendingNamesReq.asString.body).validate[Movie]

      newMovie match {
        case s: JsSuccess[Movie] =>movieDBTable.flatMap(_.insert(s.get))
        case e: JsError => println("Errors: " + JsError.toJson(e).toString())
      }
    }
    Ok("Trending Movies Added!")
  }

  def delete(name: String): Action[AnyContent] = Action.async {
    val selector = BSONDocument("name" -> name)
    val futureResult = movieDBTable.flatMap(_.remove(selector))
    futureResult.map(_ => Ok("Removed items with the name " + name))
  }


  def getTrending = {
    val response = Http("https://api.themoviedb.org/3/movie/now_playing?api_key=f675a5619b10739ad98190b5599f50d9&language=en-US&page=1")
    val currentMovies = Json.parse(response.asString.body)
    (currentMovies \"results").get.validate[List[MovieDB]].get
  }

}