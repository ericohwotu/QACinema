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
}