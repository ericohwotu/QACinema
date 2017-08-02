package controllers

import play.api._
import play.api.mvc._

class ContactController extends Controller {

  def index = Action {
    Ok(views.html.index("hello"))
  }

  def formHandler = TODO
}