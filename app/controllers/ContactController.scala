package controllers

import models.ContactUs
import play.api._
import play.api.mvc._

class ContactController extends Controller {

  def index = Action {
    Ok(views.html.index("hello"))
  }


  def formHandler = Action{ implicit request: Request[AnyContent] =>
    val formResult = ContactUs.contactForm.bindFromRequest
    formResult.fold({
      error => BadRequest(error.errors.toString())
    },{
      contactUS => Ok("Success")
    })
  }
}