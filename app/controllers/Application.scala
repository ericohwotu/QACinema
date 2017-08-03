package controllers

import play.api._
import play.api.mvc._

class Application extends Controller {

  def index = Action {
    Ok(views.html.index())
  }

  def listings = Action {
    Ok(views.html.listings())
  }

  def movie = Action {
    Ok(views.html.movie())
  }

  def about = Action {
    Ok(views.html.about())
  }

  val something = (some: Int) =>{
    10
  }
}