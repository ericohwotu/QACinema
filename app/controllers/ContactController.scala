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
        sendEmail(contactUS)
        Ok("Success")
    })
  }

  def sendEmail(contactUs: ContactUs): Unit ={
    val content = s"Raised by: ${contactUs.name}\n" +
      s"Email: ${contactUs.email} \nNumber: ${contactUs.number} \n\n" +
      s"Message: ${contactUs.message}"

    val email = Email(contactUs.subject,"qacinemainfo@gmail.com",Seq("qacinemainfo@gmail.com","daniel.ufuoma@qa.com"),Some(content))
    mailerClient.send(email)
  }
}