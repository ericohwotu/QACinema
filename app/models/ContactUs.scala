package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json

case class ContactUs(subject: String, name: String, message: String, email: String, number: String = "")

object ContactUs{
  implicit val contactUsFormat = Json.format[ContactUs]

  val contactForm: Form[ContactUs] = Form(
    mapping(
      "Subject" -> nonEmptyText,
      "Name" -> nonEmptyText,
      "Message" -> nonEmptyText,
      "Email" -> nonEmptyText,
      "Number" -> default(text,"judfjdn")
  )(ContactUs.apply _)(ContactUs.unapply _))

}
