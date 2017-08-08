package controllers


import play.api.mvc._
import javax.inject._

import play.api.data.format.Formats._
import play.api.i18n._
import util.{SeatGenerator, SessionHelper}
import com.typesafe.config.ConfigFactory
import models.{DateSelector, Screening}
import play.api.data.{Form, Forms}
import play.api.libs.concurrent.Execution.Implicits.defaultContext


@Singleton
class ScreeningsController @Inject()(implicit val messagesApi: MessagesApi,
                                     val mongoDbController: ScreeningsDbController
                                    ) extends Controller with I18nSupport{

  val paymentUrl: String = ConfigFactory.load().getString("payment.server")

  val hiddenMultips: String => List[String] = (str: String) =>  str.split(",").toList

  val homePage = (name: String, vals: List[String], request: Request[AnyContent]) =>
    Ok(views.html.bookings.bookings(name, vals)(DateSelector.dsForm, SeatGenerator.getLayout(request.remoteAddress)))


  val seatsForm: Form[(Int,Int)] = Form[(Int, Int)](
    Forms.tuple(
      "bookingid" -> Forms.of[Int],
      "seatid" -> Forms.of[Int]
    )
  )

  def index(name: String, vals: String): Action[AnyContent] = Action { request: Request[AnyContent] =>
    //create movie database
    mongoDbController.isMovieInDb(name) match {
      case true =>
        None
      case false =>
        mongoDbController.addMovie2Db(Screening.generateMovie(name))
    }

    homePage(name,hiddenMultips(vals),request)
      .withSession(request.session + ("sessionKey" -> SessionHelper.getSessionKey()) + ("movieName"->name))
  }

  def toPayment(amount: String): Action[AnyContent] = Action{request: Request[AnyContent] =>
    Redirect(routes.PaymentController.initiateClientToken())
      .withSession(request.session + ("bookingPrice" -> amount))
  }

  def toSubmitBooking: Action[AnyContent] = Action{ request: Request[AnyContent] =>
    val tDate = request.session.get("date").getOrElse("none")
    val tTime = request.session.get("time").getOrElse("none")
    Redirect(routes.ScreeningsApiController.submitBooking(date = tDate, time = tTime))
  }
}