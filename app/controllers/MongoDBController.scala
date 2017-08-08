package controllers

import javax.inject.Inject

import akka.util.LineNumbers.Result

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

import scala.collection.mutable.ArrayBuffer
import scalaj.http._

class MongoDBController @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents{

  def movieDBTable: Future[JSONCollection] = database.map(_.collection[JSONCollection]("movieDB"))
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

  def createMoviesFromAPI(): Action[AnyContent] = Action {
    movieDBTable.flatMap(_.drop(false))
    getTrending.foreach { movie =>
      val improvedFormat = movie.title.replaceAll("\\s", "+")
      val trendingNamesReq = Http("http://www.omdbapi.com/?t=" + improvedFormat + "&r=json&plot=full&apikey=313dd87a")
      val newMovie = Json.parse(trendingNamesReq.asString.body).validate[Movie]

      newMovie match {
        case s: JsSuccess[Movie] =>
          val video = Http("https://api.themoviedb.org/3/movie/"+movie.id+"/videos?api_key=f675a5619b10739ad98190b5599f50d9&language=en-US")
          val currentTrailer = Json.parse(video.asString.body)
          val trailerList = (currentTrailer \"results").get.validate[List[Trailer]].get
          s.get.video = Some(trailerList.headOption.get.key)
          movieDBTable.flatMap(_.insert(s.get))

        case e: JsError => println("Errors: " + JsError.toJson(e).toString())
      }
    }
    Ok("Trending Movies Added!")
  }


  def getTrending : List[TrendingMovie] = {
    val response = Http("https://api.themoviedb.org/3/movie/now_playing?api_key=f675a5619b10739ad98190b5599f50d9&language=en-US&page=1")
    val currentMovies = Json.parse(response.asString.body)
    (currentMovies \"results").get.validate[List[TrendingMovie]].get
  }

  def readByName(): Action[AnyContent] = Action.async {
    val name = "Kidnap"
    val cursor: Future[Cursor[Movie]] = movieDBTable.map {
      _.find(Json.obj("Title" -> name))
        .cursor[Movie]
    }
    val futureUsersList: Future[List[Movie]] = cursor.flatMap(_.collect[List]())
    futureUsersList.map { persons => Ok(persons.headOption.get.Title)
    }
  }


}