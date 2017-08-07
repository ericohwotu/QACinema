import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.libs.concurrent.Execution.Implicits._
import play.mvc.Http.RequestBuilder

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Administrator on 04/08/2017.
  */
@RunWith(classOf[JUnitRunner])
class PaymentControllerSpec extends PlaySpecification {

  "The Payments Page" should {
    "should return bad request when no amount is provided" in new WithApplication() {
      route(FakeApplication(), FakeRequest(GET, "/payment/token")) match {
        case Some(route) => status(route) must equalTo(BAD_REQUEST)
        case _ => failure
      }
    }

    "should a payment page when provided an amount" in new WithApplication() {
      route(FakeApplication(), FakeRequest(GET, "/payment/token").withSession("bookingPrice" -> "50.00")) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
        case _ => failure
      }
    }

    "trying to make a payment with no nonce or amount should be a bad request" in new WithApplication() {
      val body : List[(String, String)] = List()

      route(FakeApplication(), FakeRequest(POST, "/payment/make").withFormUrlEncodedBody(body:_*)) match {
        case Some(route) => status(route) must equalTo(BAD_REQUEST)
        case _ => failure
      }
    }

    "trying to make a payment with a fake nonce/amount should not let the transaction happen" in new WithApplication() {
      val body : List[(String, String)] = List(
        ("nonce", "test")
      )

      route(FakeApplication(), FakeRequest(POST, "/payment/make").withSession("bookingPrice" -> "50.00").withFormUrlEncodedBody(body:_*)) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentAsString(route) must contain("Transaction failure: ")
        case _ => failure
      }
    }
  }
}
