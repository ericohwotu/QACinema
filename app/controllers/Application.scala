package controllers

import javax.inject.Inject

import javax.inject.Inject

import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import play.api.libs.json._
import reactivemongo.play.json._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class Place(val xCoord: Double, val yCoord: Double, val name: String)

class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents {

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("locations"))

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def mapPage = Action {
    val list = cinemaList()
    Ok(views.html.mapdisplay(list))
  }

  def cinemaList() : List[Place] = {
    //TODO: Connect this function to the database and return the cinema coordinates in the correct format
    List(
      new Place(53.474140, -2.286074, "QACinema - Anchorage"),
      new Place(53.472101, -2.300690, "QACinema - The Heart"),
      new Place(53.487390, -2.242282, "QA Cinema - Piccadilly "),
      new Place(53.413086, -2.254408, "QA Cinema - Golf Club")
    )
  }
}