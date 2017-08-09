package controllers

import java.net.URLDecoder
import java.nio.charset.Charset
import javax.inject.Inject

import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.bson.BSONObjectID

import scala.concurrent.Future
import scala.util.{Failure, Success, Try}

class Application @Inject() (val reactiveMongoApi: ReactiveMongoApi,
                             val movieController: MovieController,
                             val locationController: LocationController)
  extends Controller with MongoController with ReactiveMongoComponents {

  //TODO: Add the hardcoded IDs and also bring the movie data with it so it is in the correct order.
  def index: Action[AnyContent] = Action.async { implicit request =>
    movieController.findMainpageMovieIDs(List("Spider-Man: Homecoming","Dunkirk",
      "Valerian and the City of a Thousand Planets",
      "War for the Planet of the Apes","Atomic Blonde","Despicable Me 3")).map {
      ids => Ok(views.html.index(ids))
    }
  }

  def listings(): Action[AnyContent] = {
    movieController.genericListingPage(None, x => x.toString)
  }

  def listingsWithGenre(genre: String): Action[AnyContent] = {
    movieController.genericListingPage(Some(genre.toLowerCase), movieController.genreExtract)
  }

  def searchByTitle(title: String): Action[AnyContent] = {
    movieController.genericListingPage(Some(URLDecoder.decode(title.toLowerCase, Charset.forName("utf-8").name())), movieController.titleExtract)
  }

  def movie(id: String): Action[AnyContent] = Action.async { implicit request =>
    BSONObjectID.parse(id) match {
      case Success(res) => movieController.getMovieAction(res)
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

  def findUs : Action[AnyContent] = Action.async {
    locationController.cinemasList.map {cinemas =>
      Ok(views.html.findUs(cinemas))
    }
  }
}
