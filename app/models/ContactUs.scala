package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

case class ContactUs(name: String, message: String, email: String)

object ContactUs{
  implicit val contactUsFormat = Json.format[ContactUs]

  val contactForm: Form[ContactUs] = Form(
    mapping("name" -> nonEmptyText,
    "message" -> nonEmptyText,
    "email" -> nonEmptyText
  )(ContactUs.apply _)(ContactUs.unapply _))

}
