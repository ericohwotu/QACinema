package controllers

import javax.inject.Inject

import models.User
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
                                val reactiveMongoApi: ReactiveMongoApi
                              ) extends Controller with I18nSupport
  with ReactiveMongoComponents with MongoController {

  def usersCol: Future[JSONCollection] = database.map(_.collection[JSONCollection]("UserCollection"))

  def addUserToDb(user: User): Unit = usersCol.flatMap(_.insert(user))

  def getPassword(password: String): String = Encryption.encrypt(Encryption.getKey(password)(16),password)

  def getUsers(username: String, password: String): List[User] = {

    val pwdCheck = getPassword(password)

    val cursor:Future[Cursor[User]] = usersCol.map{
      _.find(Json.obj("username"->username, "password" -> pwdCheck))
        .cursor[User](ReadPreference.primary)
    }

    val userList: Future[List[User]] = cursor.flatMap(_.collect[List]())

    Await.result(userList, Duration.Inf)
  }

  def login(username: String, password: String):Boolean = getUsers(username, password).length == 1

  def register: Action[AnyContent] = Action {
    Ok(views.html.registration(User.registerUser))
  }

  def regHandler: Action[AnyContent] = Action {implicit request: Request[AnyContent] =>
    val regBind = User.registerUser.bindFromRequest()
    regBind.fold({
      error => BadRequest(views.html.registration(error))
    },{
      newUser => User.createUser(newUser) match {
          case None => BadRequest("Oops something happened")
          case Some(x) => addUserToDb(x)
            Ok("User Successfully added")
        }
    })
  }

  def login: Action[AnyContent] = Action {
    Ok(views.html.login(User.loginUser))
  }

  def loginHandler: Action[AnyContent] = Action {implicit request: Request[AnyContent] =>
    val loginBind = User.loginUser.bindFromRequest()
    loginBind.fold({
      error=>BadRequest(views.html.login(error))},{
      success => Ok("logged in")
    })
  }



}
