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

trait Counter {
  var id = 0
  def getID(): Int ={
    id += 1
    id
  }
}

class Mongo @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents with Counter {

  def movieTable: Future[JSONCollection] = database.map(_.collection[JSONCollection]("movies"))
  def movieDBTable: Future[JSONCollection] = database.map(_.collection[JSONCollection]("movieDB"))

//  def create: Action[AnyContent] = Action.async {
//   val newMovie = Movie(
//     getID(),
//     "Black Daniel III",
//     "18",
//     List("Action","Adventure","Thriller"),
//     "The best synopsis you've ever seen",
//     "really short description",
//     "img",
//     "video",
//     180,
//     List("JJ Abrams"),
//     List("Black Daniel", "Other Daniel", "Overlord"),
//     "01/08/2017")
//
//    val futureResult = movieTable.flatMap(_.insert(newMovie))
//    futureResult.map(_ => Ok("Added user " + newMovie.name))
//  }

  def createAPI(): Action[AnyContent] = Action {
    val genres = Http("https://api.themoviedb.org/3/genre/movie/list?api_key=f675a5619b10739ad98190b5599f50d9&language=en-US")
    val listOfGenres = Json.parse(genres.asString.body)
    val why = (listOfGenres\"genres").get.validate[List[genreConverter]].get

    getTrending.foreach { movie =>
      val updatedMovie = movie.genre_ids.map(gen => why.filter(gen2 => gen2.id == gen.toInt).head.name)

      val addMovieToDatabase = MovieDB2(
        movie.title,
        movie.backdrop_path,
        movie.overview,
        movie.poster_path,
        movie.release_date,
        updatedMovie,
        movie.vote_average
      )
       movieDBTable.flatMap(_.insert(addMovieToDatabase))}

    Ok("Success?")
  }

  def delete(name: String): Action[AnyContent] = Action.async {
    val selector = BSONDocument("name" -> name)
    val futureResult = movieTable.flatMap(_.remove(selector))
    futureResult.map(_ => Ok("Removed items with the name " + name))
  }


  def getTrending = {
    val response = Http("https://api.themoviedb.org/3/movie/now_playing?api_key=f675a5619b10739ad98190b5599f50d9&language=en-US&page=1")
    val currentMovies = Json.parse(response.asString.body)
    (currentMovies \"results").get.validate[List[MovieDB]].get
  }






}