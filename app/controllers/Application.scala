package controllers

import java.net.URLDecoder
import java.nio.charset.Charset
import javax.inject.Inject

import models.{Movie, Place}
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

class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents {

  def index: Action[AnyContent] = Action {
    Ok(views.html.index())
  }

  def locationCollection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("locations"))

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("movieDB"))

  def genreExtract(source : AnyRef) : String = source match {
    case jso : JsObject => (jso \ "Genre").as[String]
    case mov : Movie => mov.Genre
  }
  def titleExtract(source : AnyRef) : String = source match {
    case jso : JsObject => (jso \ "Title").as[String]
    case mov : Movie => mov.Title
  }

  def futureRefinedList[A <: AnyRef](criteria : Option[String], method : (AnyRef) => String, jsonObj : JsObject)(implicit reads: Reads[A]) : Future[List[A]] = {
    val collectedList : Future[List[A]] = collection.map {
      _.find(Json.obj(), jsonObj).cursor[A]()
    }.flatMap(_.collect[List]())

    criteria.fold(collectedList)(crit => collectedList.flatMap(movieList => Future {
      movieList.filter(mov => method(mov).toLowerCase.contains(crit))
    }))
  }

  def genericListingPage(criteria: Option[String], method : (AnyRef) => String): Action[AnyContent] = Action.async { implicit request =>
    futureRefinedList[JsObject](criteria, method, Json.obj("_id" -> 1, "Genre" -> 1, "Title" -> 1)).flatMap(jsos =>
      futureRefinedList[Movie](criteria, method, Json.obj()).map {
        movies => Ok(views.html.listings(movies.zip(jsos.map {
          jso => ((jso \ "_id").as[JsObject] \ "$oid").as[String]
        }).grouped(3).toList))
      }
    )
  }

  def listings(): Action[AnyContent] = {
    genericListingPage(None, x => x.toString)
  }

  def listingsWithGenre(genre: String): Action[AnyContent] = {
    genericListingPage(Some(genre.toLowerCase), genreExtract)
  }

  def searchByTitle(title: String): Action[AnyContent] = {
    genericListingPage(Some(URLDecoder.decode(title.toLowerCase, Charset.forName("utf-8").name())), titleExtract)
  }

  def getMovieAction(id: BSONObjectID): Future[Result] = {
    val futureIDCursor: Future[Cursor[Movie]] = collection.map {
      _.find(Json.obj("_id" -> id)).cursor[Movie]()
    }
    val futureIDList: Future[List[Movie]] = futureIDCursor.flatMap(_.collect[List]())

    futureIDList.map {
      movieIDs => movieIDs.headOption.fold(BadRequest("Movie ID does not exist."))(res => Ok(views.html.movie(res)))
    }
  }

  def movie(id: String): Action[AnyContent] = Action.async { implicit request =>
    BSONObjectID.parse(id) match {
      case Success(res) => getMovieAction(res)
      case Failure(err) => Future {
        BadRequest("Invalid ID")
      }
    }
  }

  def about: Action[AnyContent] = Action {
    Ok(views.html.about())
  }

  def certifications: Action[AnyContent] = Action {
    Ok(views.html.certifications())
  }

  def findUs: Action[AnyContent] = Action.async { implicit request =>
    val cursor: Future[Cursor[Place]] = locationCollection.map {
      _.find(Json.obj()).cursor[Place]
    }
    val futureCinemaList: Future[List[Place]] = cursor.flatMap(_.collect[List]())
    futureCinemaList.map { cinemas =>
      Ok(views.html.findUs(cinemas))
    }
  }
}
