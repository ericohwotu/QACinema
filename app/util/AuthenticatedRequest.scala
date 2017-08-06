package util

import javax.inject.{Inject, Singleton}

import controllers.{UserController, routes}
import models.User
import play.api.mvc._

import scala.concurrent.Future

class AuthenticatedRequest[R](user: User, request: Request[R]) extends WrappedRequest[R](request)

//class AuthenticatedAction @Inject()(userController: UserController) extends ActionBuilder[AuthenticatedRequest]{
//  def invokeBlock[R](request: Request[R], block: AuthenticatedRequest[R] => Future[Result]): Future[Result] = {
//    request.session.get("loggedin")
//      .flatMap(userController.getUsers(_).head.asInstanceOf[Option[User]])
//      .map(user => block(new AuthenticatedRequest(user, request)))
//      .headOption.getOrElse(Future.successful(Results.Redirect(routes.UserController.login())))
//  }
//}
