package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import util.{Encryption, SessionHelper}

case class User(name: String, username: String, password: String,
                 email: String, bookings: List[Booking] = List(),
                  favourites: List[Movie] = List(),
                  membership: String="",points: Long, isAdmin: Boolean = false)



object User {
  implicit val userFormat = Json.format[User]

  val loginUser = Form(
    tuple(
      "Username" -> nonEmptyText,
      "Password" -> nonEmptyText
    ))

  def create(newUser: (String, String, String, String, String)): Option[User] = newUser match{
    case (name, uName, pwd, cPwd, em) =>
      val byteLimit = 16
      val initPoints = 1000
      val membership = SessionHelper.getSessionKey(byteLimit)
      val key = Encryption.getKey(pwd)(byteLimit)
      Some(User(name,uName, Encryption.encrypt(key,pwd), em, List(),List(),membership,initPoints))
    case _ => None
  }
}
