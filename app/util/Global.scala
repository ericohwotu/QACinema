package util

import play.api._
import play.api.mvc.{RequestHeader, Result}
import play.api.mvc.Results._

import scala.concurrent.Future

object Global extends GlobalSettings {
  override def onHandlerNotFound(request: RequestHeader): Future[Result] = {
    Future.successful(NotFound(views.html.notFound(request.path)))
  }
}