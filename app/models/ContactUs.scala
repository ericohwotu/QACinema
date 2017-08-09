package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json._

case class ContactUs(sub: String, name: String, message: String, email: String, number: String = "")

object ContactUs{
  implicit val contactUsFormat: OFormat[ContactUs] = Json.format[ContactUs]

  val contactForm: Form[ContactUs] = Form(
    mapping(
      "Subject" -> nonEmptyText,
      "Name" -> nonEmptyText,
      "Message" -> nonEmptyText,
      "Email" -> nonEmptyText,
      "Number" -> default(text,"none")
  )(ContactUs.apply)(ContactUs.unapply))

}
