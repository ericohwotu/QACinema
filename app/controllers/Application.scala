package controllers

import javax.inject.Inject
import models.Place
import scala.concurrent.Future
import play.api.mvc.{Action, AnyContent, Controller}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json._
import reactivemongo.api.Cursor
//import models.JsonFormats._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._
import collection._

class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents {

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("locations"))

  def index = Action {
    Ok(views.html.index("Your new application is ready."))
  }

  def mapPage: Action[AnyContent] = Action.async { implicit request =>
    val cursor: Future[Cursor[Place]] = collection.map {
      _.find(Json.obj()).cursor[Place]
    }
    val futureCinemaList: Future[List[Place]] = cursor.flatMap(_.collect[List]())
    futureCinemaList.map { cinemas =>
      Ok(views.html.mapdisplay(cinemas))
    }
  }

  /*def cinemaList() : List[Place] = {
    //TODO: Connect this function to the database and return the cinema coordinates in the correct format
    List(
      new Place(53.474140, -2.286074, "QACinema - Anchorage"),
      new Place(53.472101, -2.300690, "QACinema - The Heart"),
      new Place(53.487390, -2.242282, "QA Cinema - Piccadilly "),
      new Place(53.413086, -2.254408, "QA Cinema - Golf Club")
    )
  }*/
}