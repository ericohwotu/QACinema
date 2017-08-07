package controllers

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

  def collectionSearch[A](jsonObj : JsObject)(implicit reads: Reads[A]) : Future[List[A]] = {
    val futureCursor: Future[Cursor[A]] = collection.map {
      _.find(Json.obj(), jsonObj).cursor[A]()
    }
    futureCursor.flatMap(_.collect[List]())
  }

  def futureMovies(criteria: Option[String], method : (AnyRef) => String): Future[List[Movie]] = {
    val collectedList : Future[List[Movie]] = collectionSearch[Movie](Json.obj())

    criteria.fold(collectedList)(crit => collectedList.flatMap(movieList => Future {
      movieList.filter(mov => method(mov).toLowerCase.contains(crit))
    }))
  }

  def futureIDs(criteria: Option[String], method : (AnyRef) => String): Future[List[JsObject]] = {
    val collectedList : Future[List[JsObject]] = collectionSearch[JsObject](
      Json.obj("_id" -> 1, "Genre" -> 1, "Title" -> 1)
    )

    criteria.fold(collectedList)(crit => collectedList.flatMap(jsos => Future {
      jsos.filter(jso => method(jso).toLowerCase.contains(crit))
    }))
  }

  def listingsPage(genre: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    futureIDs(genre, genreExtract).flatMap(jsos => {
      futureMovies(genre, genreExtract).map {
        movies => {
          Ok(views.html.listings(movies.zip(jsos.map {
            jso => ((jso \ "_id").as[JsObject] \ "$oid").as[String]
          }).grouped(3).toList))
        }
      }
    })
  }

  def searchPage(title: Option[String]): Action[AnyContent] = Action.async { implicit request =>
    futureIDs(title, titleExtract).flatMap(jsos => {
      futureMovies(title, titleExtract).map {
        movies => {
          Ok(views.html.listings(movies.zip(jsos.map {
            jso => ((jso \ "_id").as[JsObject] \ "$oid").as[String]
          }).grouped(3).toList))
        }
      }
    })
  }

  def listings(): Action[AnyContent] = {
    listingsPage(None)
  }

  def listingsWithGenre(genre: String): Action[AnyContent] = {
    listingsPage(Some(genre.toLowerCase))
  }

  def searchByTitle(title: String): Action[AnyContent] = {
    searchPage(Some(title.toLowerCase))
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

  def nearby: Action[AnyContent] = Action {
    Ok(views.html.nearby())
  }
}
