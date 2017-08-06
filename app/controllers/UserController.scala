package controllers

import javax.inject.Inject

import models.User
import play.api.cache.{CacheApi, Cached}
import play.api.data.Form
import play.api.data.Forms._
import play.api.i18n.{I18nSupport, MessagesApi}
import play.api.libs.json.Json
import play.api.mvc.Controller
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
                                val userCache: CacheApi
                              ) extends Controller with I18nSupport
  with ReactiveMongoComponents with MongoController {

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

  def addUserToDb(user: User): Unit = usersCol.flatMap(_.insert(user))

  def getPassword(password: String): String = Encryption.encrypt(Encryption.getKey(password)(16),password)

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

  def register: Action[AnyContent] = Action {
    Ok(views.html.users.registration(registerUser))
  }

  def regHandler: Action[AnyContent] = Action {implicit request: Request[AnyContent] =>
    val regBind = registerUser.bindFromRequest()
    regBind.fold({
      error => BadRequest(views.html.users.registration(error))
    },{
      newUser => User.createUser(newUser) match {
          case None => BadRequest("Oops something happened")
          case Some(x) => addUserToDb(x)
            Ok("User Successfully added")
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
        val user = getUserByPassword(uName,pwd).head
        userCache.set("loggedin", user)
        Redirect(routes.UserController.dashboard())
    })
  }

  def dashboard:Action[AnyContent] = Action {
    userCache.get[User]("loggedin").fold{
      Unauthorized("sorry you are not logged in")
    }{
      user => Ok(views.html.users.dashboard(user))
    }
  }



}
