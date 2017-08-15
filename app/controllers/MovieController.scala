package controllers

import javax.inject.Inject

import play.api.libs.json.{JsObject, Json, Reads}
import play.api.mvc._
import models.{Movie, MovieSearch}
import play.api.data.Form
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json._
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.bson.BSONObjectID
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

class MovieController @Inject() (val reactiveMongoApi: ReactiveMongoApi) extends Controller with MongoController with ReactiveMongoComponents {

  def collection: Future[JSONCollection] = database.map(_.collection[JSONCollection]("movieDB"))
  def getMongoID(jso : JsObject): String = ((jso \ "_id").as[JsObject] \ "$oid").as[String]

  def applyCriteriaOnQuery[A <: AnyRef](collectedList : Future[List[A]], criteria : String, method : (AnyRef) => String) : Future[List[A]] =
    collectedList.flatMap(movieList => Future {
      movieList.filter(mov => method(mov).toLowerCase.contains(criteria))
    })

  def futureRefinedList[A <: AnyRef](criteria : Option[String], method : (AnyRef) => String, query: JsObject, returnData : JsObject)(implicit reads: Reads[A]) : Future[List[A]] = {
    val collectedList : Future[List[A]] = collection.map {
      _.find(query, returnData).cursor[A](ReadPreference.primary)
    }.flatMap(_.collect[List](-1, Cursor.FailOnError[List[A]]()))

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

  def getMovieAction(id: BSONObjectID): Future[Result] = {
    val futureIDCursor: Future[Cursor[Movie]] = collection.map {
      _.find(Json.obj("_id" -> id)).cursor[Movie]()
    }
    val futureIDList: Future[List[Movie]] = futureIDCursor.flatMap(_.collect[List](-1, Cursor.FailOnError[List[Movie]]()))

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

  def movieSearchForm() : Form[MovieSearch] = MovieSearch.movieSearchForm

  def takeMovieSearchForm()(implicit request : Request[AnyContent]) : Future[List[List[(Movie, String)]]] = {
    MovieSearch.movieSearchForm.bindFromRequest.fold(
      errs => Future {List()},
      moviesearch => futureRefinedList[JsObject](None, x => x.toString, moviesearch.jsonObject, Json.obj("_id" -> 1)).flatMap(jsos =>
        futureRefinedList[Movie](None, x => x.toString, moviesearch.jsonObject, Json.obj()).map {
          movies => movies.zip(jsos.map {
            jso => getMongoID(jso)
          }).grouped(3).toList
        }
      )
    )
  }
}
