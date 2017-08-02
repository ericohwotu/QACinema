package controllers

import javax.inject.Inject

import models.ContactUs
import play.api._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.mailer._
import play.api.mvc._

class ContactController @Inject()(implicit val messagesApi: MessagesApi, mailerClient: MailerClient)
  extends Controller with I18nSupport{

  def index = Action {
    Ok(views.html.contactform(ContactUs.contactForm))
  }


  def formHandler = Action{ implicit request: Request[AnyContent] =>
    val formResult = ContactUs.contactForm.bindFromRequest
    formResult.fold({
      error => BadRequest(views.html.contactform(error))
    },{
      contactUS =>
        sendEmail(contactUS.email, contactUS.message)
        Ok("Success")
    })
  }

  def sendEmail(from: String, msg: String): Unit ={
    val email = Email("Contact","james.mountain@qa.com",Seq("","eric.ohwotu@qa.com","adam.clark@qa.com",
      "james.mountain@qa.com","Daniel.Ufuoma@qa.com","Ladon.Jackson@qa.com","alex.Rimmer@qa.com","lewis.hay@qa.com"),Some(msg))
    mailerClient.send(email)
  }
}