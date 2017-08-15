package controllers

import javax.inject.Inject

import scala.concurrent.Future
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import reactivemongo.api.Cursor
import models._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._
import collection._
import reactivemongo.bson.{BSONDocument, BSONObjectID}

import scala.collection.mutable.{ArrayBuffer, ListBuffer}
import scalaj.http._

class MongoDBController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents{

  def cinemaLocationsTable: Future[JSONCollection] = database.map(_.collection[JSONCollection]("locations"))

  def addLocations(): Action[AnyContent] = Action {
    cinemaLocationsTable.flatMap(_.drop(false))
    val locations = ArrayBuffer(
      CinemaLocation("QACinema", "53.4695009","-2.292369"),
      CinemaLocation("QA DockCinema", "53.4741912","-2.2871522"),
      CinemaLocation("QA Theatre","53.4449796","-2.2205691"),
      CinemaLocation("QA FilmCentre","53.4261648","-2.5255555")
    )

    locations.foreach{loc =>
    val futureResult = cinemaLocationsTable.flatMap(_.insert(loc))
    futureResult.map(_ => Ok("Added location"))}
    Ok("success")
  }

def dropDatabases(listOfCinemas: List[String]): Unit ={
  listOfCinemas.foreach(cin => database.map(_.collection[JSONCollection](cin)).flatMap(_.drop(false)))
}

  def createMoviesFromAPI(): Action[AnyContent] = Action {
    val trendingList = getTrending
    val listOfCinemas = List("QACinema","QA DockCinema", "QA Theatre","QA FilmCentre")
    dropDatabases(listOfCinemas)
    var listOfMovies = ListBuffer[Movie]()

      trendingList.foreach { movie =>
        val improvedFormat = movie.title.replaceAll("\\s", "+")
        val trendingNamesReq = Http("http://www.omdbapi.com/?t=" + improvedFormat + "&r=json&plot=full&apikey=313dd87a")
        val newMovie = Json.parse(trendingNamesReq.asString.body).validate[Movie]

        newMovie match {
          case s: JsSuccess[Movie] =>
            val video = Http("https://api.themoviedb.org/3/movie/"+movie.id+"/videos?api_key=f675a5619b10739ad98190b5599f50d9&language=en-US")
            val currentTrailer = Json.parse(video.asString.body)
            (currentTrailer \"results").get.validate[List[Trailer]] match {
              case x: JsSuccess[List[Trailer]] =>  s.get.video = Some(x.get.headOption.get.key)
               listOfMovies += s.get
              case y: JsError => println("Trailer Error: " + JsError.toJson(y).toString())
            }
          case e: JsError => println("Movie Error: " + JsError.toJson(e).toString())
        }
      }

    listOfCinemas.foreach(cin =>
     listOfMovies.foreach(mov =>
       database.map(_.collection[JSONCollection](cin)).flatMap(_.insert(mov)))
    )
    Ok("Trending Movies Added!")
  }

  def getTrending : List[trendingMovieList] = {
    val response = Http("https://api.themoviedb.org/3/movie/now_playing?api_key=f675a5619b10739ad98190b5599f50d9&language=en-US&page=1")
    val currentMovies = Json.parse(response.asString.body)
    (currentMovies \"results").get.validate[List[trendingMovieList]].get
  }


}