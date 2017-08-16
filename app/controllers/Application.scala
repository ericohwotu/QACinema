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
import play.api.cache._

class Application @Inject() (@NamedCache("controller-cache") cached: Cached,
                             val reactiveMongoApi: ReactiveMongoApi,
                             val movieController: MovieController,
                             val locationController: LocationController)
  extends Controller with MongoController with ReactiveMongoComponents {

  //TODO: Add the hardcoded IDs and also bring the movie data with it so it is in the correct order.
  def index: EssentialAction = cached("index") {
    Action.async { implicit request =>
      movieController.findMainpageMovieIDs(List()).map {
        ids => Ok(views.html.index(ids))
      }
    }
  }

  def listings: EssentialAction = cached("listings") {
    movieController.genericListingPage(None, x => x.toString)
  }

  def listingsWithGenre(genre: String): EssentialAction = cached("listings_" + genre.toLowerCase) {
    movieController.genericListingPage(Some(genre.toLowerCase), movieController.genreExtract)
  }

  def searchByTitle(title: String): EssentialAction = cached("search_" + title.toLowerCase) {
    movieController.genericListingPage(Some(URLDecoder.decode(title.toLowerCase, Charset.forName("utf-8").name())), movieController.titleExtract)
  }

  def movie(id: String): EssentialAction = cached("movie_" + id) {
    Action.async { implicit request =>
      BSONObjectID.parse(id) match {
        case Success(res) => movieController.getMovieAction(res)
        case Failure(err) => Future {
          BadRequest("Invalid ID")
        }
      }
    }
  }

  def about: Action[AnyContent] = Action {
    Ok(views.html.about())
  }

  def certifications: Action[AnyContent] = Action {
    Ok(views.html.certifications())
  }

  def findUs: Action[AnyContent] = Action.async {
    locationController.cinemasList.map { cinemas =>
      Ok(views.html.findUs(cinemas))
    }
  }
}
