package controllers

import models.Movie
import play.api._
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def listings = Action {
    Ok(views.html.listings(Movie.movies.toList.grouped(3).toList))
  }

  def movie = Action {
    Ok(views.html.movie())
  }
}