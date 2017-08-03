package controllers

import javax.inject.Inject

import models.Movie
import play.api.libs.json.Json
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import play.api.libs.json._
import reactivemongo.play.json._
import reactivemongo.api.Cursor
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

class Application @Inject()(val reactiveMongoApi : ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents  {

  def index = Action {
    Ok(views.html.index())
  }

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("movieDB"))
  def futureMovies: Future[List[Movie]] = {
    val futureCursor: Future[Cursor[Movie]] = collection.map {_.find(Json.obj()).cursor[Movie]()}

    futureCursor.flatMap(_.collect[List]())
  }

  def listings: Action[AnyContent] = Action.async { implicit request =>
    futureMovies.map {
      movies => Ok(views.html.listings(movies.grouped(3).toList))
    }
  }

  def movie = Action {
    Ok(views.html.movie())
  }
}