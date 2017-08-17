package controllers

import javax.inject.Inject

import play.api.libs.json.{JsArray, JsObject, Json, Reads}
import play.api.mvc.{Action, AnyContent, Controller, Result}
import models.Movie
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

class MovieController @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents {


  def getMongoID(jso : JsObject): String = ((jso \ "_id").as[JsObject] \ "$oid").as[String]

  def applyCriteriaOnQuery[A <: AnyRef](collectedList : Future[List[A]], criteria : String, method : (AnyRef) => String) : Future[List[A]] =
    collectedList.flatMap(movieList => Future {
      movieList.filter(mov => method(mov).toLowerCase.contains(criteria))
    })

  def futureRefinedList[A <: AnyRef](criteria : Option[String], method : (AnyRef) => String, query: JsObject, returnData : JsObject)(implicit reads: Reads[A], cinemaName: String) : Future[List[A]] = {
    def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection](cinemaName))
    val collectedList : Future[List[A]] = collection.map {
      _.find(query, returnData).cursor[A]()
    }.flatMap(_.collect[List]())

    criteria.fold(collectedList)(crit => applyCriteriaOnQuery[A](collectedList, crit, method))
  }

  def genreExtract(source : AnyRef) : String = source match {
    case jso : JsObject => (jso \ "Genre").as[String]
    case mov : Movie => mov.Genre
  }
  def titleExtract(source : AnyRef) : String = source match {
    case jso : JsObject => (jso \ "Title").as[String]
    case mov : Movie => mov.Title
  }

  def genericListingPage(criteria: Option[String], method : (AnyRef) => String): Action[AnyContent] = Action.async { implicit request =>
    futureRefinedList[JsObject](criteria, method, Json.obj(), Json.obj("_id" -> 1, "Genre" -> 1, "Title" -> 1)).flatMap(jsos =>
      futureRefinedList[Movie](criteria, method, Json.obj(), Json.obj()).map {
        movies => Ok(views.html.listings(movies.zip(jsos.map {
          jso => getMongoID(jso)
        }).grouped(3).toList))
      }
    )
  }

  def getMovieAction(id: BSONObjectID, cinemaName: String): Future[Result] = {
    def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection](cinemaName))
    val futureIDCursor: Future[Cursor[Movie]] = collection.map {
      _.find(Json.obj("_id" -> id)).cursor[Movie]()
    }
    val futureIDList: Future[List[Movie]] = futureIDCursor.flatMap(_.collect[List]())

    futureIDList.map {
      movieIDs => movieIDs.headOption.fold(BadRequest(views.html.noMovie()))(res => Ok(views.html.movie(res)))
    }
  }

  def findMainpageMovieIDs(movieTitleStrings : List[String]) : Future[List[String]] = {
    futureRefinedList[JsObject](None, x => x.toString,
      Json.obj("Title" -> Json.obj("$in" -> Json.toJson(movieTitleStrings))),
      Json.obj("_id" -> 1)).map(jsos => jsos.map {
      jso => getMongoID(jso)
    })
  }

}
