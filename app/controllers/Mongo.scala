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

trait Counter {
  def getID(lastID: Int): Int ={
    val newID = lastID + 1
    newID
  }
}

class Mongo @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents with Counter {

  def movieTable: Future[JSONCollection] = database.map(_.collection[JSONCollection]("movies"))

  def create: Action[AnyContent] = Action.async {
   val newMovie = Movie(
     getID(0),
     "Black Daniel III",
     "18",
     List("Action","Adventure","Thriller"),
     "The best synopsis you've ever seen",
     "really short description",
     "img",
     "video",
     180,
     List("JJ Abrams"),
     List("Black Daniel", "Other Daniel", "Overlord"),
     "01/08/2017")

    val futureResult = movieTable.flatMap(_.insert(newMovie))
    futureResult.map(_ => Ok("Added user " + newMovie.name))
  }

  def delete(name: String): Action[AnyContent] = Action.async {
    val selector = BSONDocument("name" -> name)
    val futureResult = movieTable.flatMap(_.remove(selector))
    futureResult.map(_ => Ok("Removed items with the name " + name))
  }



}