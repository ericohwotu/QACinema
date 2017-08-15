package controllers

import javax.inject.Inject
import java.io.{ByteArrayInputStream, ByteArrayOutputStream, ObjectOutputStream}
import javax.inject.Inject

import scala.concurrent.ExecutionContext.Implicits.global
import reactivemongo.bson.BSONDocument
import play.api.http.HttpEntity
import models.Movie
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import HttpEntity.Streamed
import akka.actor.FSM.Failure
import akka.actor.Status.Success
import akka.stream.scaladsl.{FileIO, Source, StreamConverters}
import akka.util.ByteString
import models.Movie
import play.api.cache._
import play.api.libs.json.Json
import play.api.libs.json._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.Future

/**
  * Created by Administrator on 07/08/2017.
  */
class Admin @Inject()( @NamedCache("controller-cache") cachedCon: Cached,
                       @NamedCache("document-cache") cached: CacheApi,
                       val reactiveMongoApi: ReactiveMongoApi,
                       val messagesApi: MessagesApi)
  extends Controller with MongoController with ReactiveMongoComponents with I18nSupport {

  def movieDBTable: Future[JSONCollection] = cached.getOrElse[Future[JSONCollection]]("movieCollection") {
    database.map(_.collection[JSONCollection]("movieDB"))
  }

  def getAllMovies: EssentialAction = cachedCon("getallmovies") {
    Action.async {
      val cursor: Future[Cursor[Movie]] = movieDBTable.map {
        _.find(Json.obj())
          .cursor[Movie]
      }

      val futureUsersList: Future[List[Movie]] = cursor.flatMap(_.collect[List]())
      futureUsersList.map { persons => Ok(views.html.admin(persons, Movie.createMovieForm))
      }
    }
  }

  def createMovie :Action[AnyContent] = Action { implicit request =>
    val formValidationResult = Movie.createMovieForm.bindFromRequest
    formValidationResult.fold({ formWithErrors =>
      BadRequest("Invalid data in form")
    }, { movie =>
      create(movie)
      Redirect(routes.Admin.getAllMovies())
    })
  }


  def updateMovie :Action[AnyContent] = Action { implicit request =>
    val formValidationResult = Movie.createMovieForm.bindFromRequest
    formValidationResult.fold({formWithErrors =>
      BadRequest("Invalid data in form")
    }, {newMovie =>
      println(newMovie)
      val selector = Json.obj("Title" -> newMovie.Title)
      val modifier = Json.obj(
        "Title" -> newMovie.Title,
        "Rated" -> newMovie.Rated,
        "Released" -> newMovie.Released,
        "Runtime" -> newMovie.Runtime,
        "Genre" -> newMovie.Genre,
        "Director" -> newMovie.Director,
        "Actors" -> newMovie.Actors,
        "Plot" -> newMovie.Plot,
        "Poster" -> newMovie.Poster,
        "video" -> newMovie.video
      )

      movieDBTable.map(_.findAndUpdate(selector, modifier))
      Redirect(routes.Admin.getAllMovies())
    })
  }


  def create(newMovie: Movie):  Unit = {
    movieDBTable.flatMap(_.insert(newMovie))
  }

  def delete(name: String): Action[AnyContent] = Action.async {
    val futureResult = movieDBTable.map(_.findAndRemove(Json.obj("Title" -> name)))
    futureResult.map(_ =>  Redirect(routes.Admin.getAllMovies()))
  }


}
