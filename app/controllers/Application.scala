package controllers

import java.net.URLDecoder
import java.nio.charset.Charset
import javax.inject.Inject

import play.api.cache._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.mvc._
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import reactivemongo.bson.BSONObjectID

import scala.concurrent.duration.Duration
import scala.concurrent.{Await, Future}
import scala.util.{Failure, Success}

class Application @Inject() (@NamedCache("controller-cache") cached: Cached,
                             val movieController: MovieController,
                             val locationController: LocationController,
                             implicit val messagesApi: MessagesApi)
  extends Controller with I18nSupport {

  def deconstructMainpageElems[A](rem: List[Future[A]], elems : List[A]) : List[A] = rem match {
    case Nil => elems
    case element :: other => deconstructMainpageElems(other, Await.result(element, Duration.Inf) match {
      case elem => elems :+ elem
    })
  }

  def index: EssentialAction = cached("index") {
    Action {
      val carelems: List[(String, String)] = deconstructMainpageElems(movieController.findMainpageCarouselMovies, List())
      val preids: List[String] = deconstructMainpageElems(movieController.findMainpagePreviewIDs, List())

      Ok(views.html.index(carelems, preids))
    }
  }

  def listings(): EssentialAction = cached("listings") {
    Action.async {
      movieController.genericListingPage(None, x => x.toString).map { movies =>
        Ok(views.html.listings(movies))
      }
    }
  }

  def listingsWithGenre(genre: String): EssentialAction = cached("listings_" + genre.toLowerCase) {
    Action.async {
      movieController.genericListingPage(Some(genre.toLowerCase), movieController.genreExtract).map { movies =>
        Ok(views.html.listings(movies))
      }
    }
  }

  def listingsByTitle(title: String): EssentialAction = cached("listing_" + title.toLowerCase) {
    Action.async {
      movieController.genericListingPage(Some(URLDecoder.decode(title.toLowerCase, Charset.forName("utf-8").name())), movieController.titleExtract).map { movies =>
        Ok(views.html.listings(movies))
      }
    }
  }

  def searchPage: Action[AnyContent] = Action {
    Ok(views.html.search(List(), movieController.movieSearchForm(), None))
  }

  def richSearch() : Action[AnyContent] = Action.async {implicit request =>
    movieController.takeMovieSearchForm.map {
      results => Ok(views.html.search(results, movieController.movieSearchForm(),
        movieController.movieSearchForm().bindFromRequest.fold(
          errs => Some("An error occured in your search."),
          moviesearch => Some("Search query - " + moviesearch.toString)
        )
      ))
    }
  }

  def movie(id: String): EssentialAction = cached("movie_" + id) {
    Action.async { implicit request =>
      BSONObjectID.parse(id) match {
        case Success(res) => movieController.getMovieAction(res).map {
          option => option.fold(BadRequest(views.html.noMovie()))(res => Ok(views.html.movie(res)))
        }
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