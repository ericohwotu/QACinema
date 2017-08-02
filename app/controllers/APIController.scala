package controllers

import models.Movie
import play.api.mvc._

class APIController extends Controller {
  def movieByID(id: Int) : Action[AnyContent] = Action {
    Movie.movies.find(movie => id == movie.id) match {
      case Some(movie) => Ok(views.html.moviepage(movie))
      case None => Ok("No movie found!")
    }
  }

  def movies() : Action[AnyContent] = Action {
    Ok(views.html.movies(Movie.movies.toList))
  }
}