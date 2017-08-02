package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

case class ContactUs(name: String, message: String, email: String, number: String)

object ContactUs{
  implicit val contactUsFormat = Json.format[ContactUs]

  val contactForm: Form[ContactUs] = Form(
    mapping("Name" -> nonEmptyText,
    "Message" -> nonEmptyText,
    "Email" -> nonEmptyText,
      "Number" -> text
  )(ContactUs.apply _)(ContactUs.unapply _))

}
