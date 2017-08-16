package controllers

import javax.inject.Inject

import models.{Booking, User}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc._
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.{Cursor, ReadPreference}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._
import util.Encryption

import scala.concurrent.duration.Duration
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.{Await, Future}

class UserController @Inject()(
                                implicit val messagesApi: MessagesApi,
                                val reactiveMongoApi: ReactiveMongoApi,
                                screeningsDbController: ScreeningsDbController
                              ) extends Controller with I18nSupport
  with ReactiveMongoComponents with MongoController {

  val byteLength = 16
  val minPwdLen = 6

  val registerUser = Form(
    tuple(
      "Name" -> nonEmptyText,
      "Username" -> nonEmptyText,
      "Password" -> nonEmptyText.verifying("Between 6 and 16 characters", pwd=>
        pwd.length >= minPwdLen && pwd.length <= byteLength ),
      "Confirm Password" -> nonEmptyText,
      "Email" -> nonEmptyText
    ) verifying("Password Must Match",fields => fields match {
      case (_, _, pwd, cPwd, _) => pwd == cPwd
    })
      verifying("Valid email required", fields => fields match{
      case (_, _, _, _, em) => em.contains("@")
    })
    verifying("Username Exists",fields => fields match{
      case (_,uName,_,_,_) => getUsers(uName).length != 1
    })
  )

  val loginUser = Form(
    tuple(
      "Username" -> nonEmptyText,
      "Password" -> nonEmptyText
    ) verifying ("Username or Password is Incorrect", fields => fields match {
      case (uName, pwd) => validateLogin(uName, pwd)
    })
  )

  def usersCol: Future[JSONCollection] = database.map(_.collection[JSONCollection]("UserCollection"))

  def bookingsCol: Future[JSONCollection] = database.map(_.collection[JSONCollection]("AnonBookingsCollection"))

  def addUserToDb(user: User): Unit = usersCol.flatMap(_.insert(user))

  def getPassword(password: String): String = Encryption.encrypt(Encryption.getKey(password)(byteLength),password)

  def getUsers(username: String): List[User] = {

    val cursor:Future[Cursor[User]] = usersCol.map{
      _.find(Json.obj("username"->username))
        .cursor[User](ReadPreference.primary)
    }

    val userList: Future[List[User]] = cursor.flatMap(_.collect[List]())

    Await.result(userList, Duration.Inf)
  }

  def getUserByPassword(username: String, password: String): List[User] = getUsers(username)
    .filter(_.password == getPassword(password))

  def validateLogin(username:String, password: String): Boolean = getUserByPassword(username, password).length == 1


  def addBooking(username: Option[String], booking: Booking): Unit = username.fold{
    addBookingToDb(booking)
  }{
    uName => addBookingToUser(uName, booking)
  }

  def addBookingToDb(booking: Booking): Unit = bookingsCol.flatMap(_.insert(booking))

  def addBookingToUser(username: String, booking: Booking): Unit = {
    usersCol.map {
      _.update(Json.obj("username" -> username), Json.obj("$push" ->
        Json.obj("bookings" -> Json.obj("$each" -> List(booking)))))
    }

    updatePoints(username, booking.price)
  }

  def calculatePoints(username: String, cost: Double): Long = {
    val baseConverter = 5
    Math.floor(cost/baseConverter).toLong + getUsers(username).headOption.orNull.points
  }

  def alterUserPoints(username: String, points: Long): Unit = usersCol.map{
    _.update(Json.obj("username"-> username),Json.obj(
      "$set"-> Json.obj("points" -> points)))
  }

  def updatePoints(username: String, cost: Double): Unit ={
    val newPoints = calculatePoints(username,cost)
    alterUserPoints(username,newPoints)
  }

  def register: Action[AnyContent] = Action {
    Ok(views.html.users.registration(registerUser))
  }

  def regHandler: Action[AnyContent] = Action {implicit request: Request[AnyContent] =>
    val regBind = registerUser.bindFromRequest()
    regBind.fold({
      error => BadRequest(views.html.users.registration(error))
    },{
      newUser => User.create(newUser) match {
          case None => BadRequest("Oops something happened")
          case Some(x) => addUserToDb(x)
            Redirect(routes.UserController.login())
        }
    })
  }

  def login: Action[AnyContent] = Action {
    Ok(views.html.users.login(User.loginUser))
  }

  def loginHandler: Action[AnyContent] = Action {implicit request: Request[AnyContent] =>
    val loginBind = loginUser.bindFromRequest()
    loginBind.fold({
      error=>
        BadRequest(views.html.users.login(error))},{
      case (uName, pwd) =>
        getUserByPassword(uName,pwd).headOption.orNull
        Redirect(routes.UserController.dashboard())
          .withSession(request.session + ("loggedin"->uName))
    })
  }

  def dashboard: Action[AnyContent] = Action { request: Request[AnyContent] =>
    request.session.get("loggedin").fold{
      Redirect(routes.UserController.login())
    }{
      username =>
        getUsers(username).headOption.fold{
          Redirect(routes.UserController.login())
        } {
          user => Ok(views.html.users.dashboard(user, screeningsDbController.getRecommendations(user.bookings)))
        }
    }
  }

  def logout(): Action[AnyContent] = Action {
    Redirect(routes.Application.index()).withNewSession
  }

  def delete(username: String): Action[AnyContent] = Action { request: Request[AnyContent] =>
    request.session.get("isTest").fold{ Unauthorized("Sorry Functionality not available to you")}{
      value => deleteUser(username)
        Redirect(routes.UserController.logout())
    }
  }

  def deleteUser(username: String): Unit = Await.result(usersCol.map {
    _.findAndRemove(Json.obj("username"->username))}, Duration.Inf)


}
