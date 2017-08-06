package models

import play.api.data.Form
import play.api.data.Forms._
import play.api.libs.json.Json
import util.{Encryption, SessionHelper}

case class User(name: String, username: String, password: String,
                 email: String, bookings: List[Bookings] = List(),
                  favourites: List[Movie] = List(),
                  membership: String="", isAdmin: Boolean = false)



object User {
  implicit val userFormat = Json.format[User]

  val registerUser = Form(
    tuple(
    "Name" -> nonEmptyText,
    "Username" -> nonEmptyText,
    "Password" -> nonEmptyText,
    "Confirm Password" -> nonEmptyText,
    "Email" -> nonEmptyText
  ) verifying("Password Must Match",fields => fields match {
      case (_, _, pwd, cPwd, _) => pwd == cPwd
    })
    verifying("Valid email required", fields => fields match{
      case (_, _, _, _, em) => em.contains("@")
    })
  )

  val loginUser = Form(
    tuple(
      "Username" -> nonEmptyText,
      "Password" -> nonEmptyText
    ))

  def createUser(newUser: (String, String, String, String, String)): Option[User] = newUser match{
    case (name, uName, pwd, cPwd, em) =>
      val key = Encryption.getKey(pwd)(16)
      Some(User(name,uName, Encryption.encrypt(key,pwd), em, List(),List(),SessionHelper.getSessionKey(12)))
    case _ => None
  }
}
