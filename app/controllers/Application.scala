package controllers

import play.api._
import play.api.mvc._

class Application extends Controller {

  def index() = Action {
    Ok(views.html.index())
  }

  def certifications() = Action {
    Ok(views.html.certifications())
  }

  def contactUs() = Action {
    Ok(views.html.contactUs())
  }

  def findUs() = Action {
    Ok(views.html.findUs())
  }

}
