import org.specs2.runner._
import org.junit.runner._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.libs.concurrent.Execution.Implicits._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Created by Administrator on 04/08/2017.
  */
@RunWith(classOf[JUnitRunner])
class PaymentControllerSpec extends PlaySpecification {

  "The Payments Page" should {
    implicit val app = FakeApplication()

    "should return bad request when no amount is provided" in {
      val home = route(app, FakeRequest(GET, "/payment/token")).get

      status(home) must equalTo(BAD_REQUEST)
    }

    "should a payment page when provided an amount" in {
      val home = route(app, FakeRequest(GET, "/payment/token?amount=50")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
    }

    "trying to make a payment with no nonce or amount should be a bad request" in new WithApplication() {
      val body : List[(String, String)] = List()

      val Some(result) = route(FakeRequest(POST, "/payment/make").withFormUrlEncodedBody(body:_*))
      status(result) must equalTo(BAD_REQUEST)
    }

    "trying to make a payment with a fake nonce/amount should not let the transaction happen" in new WithApplication() {
      val body : List[(String, String)] = List(
        ("nonce", "test"),
        ("amount", "50.00")
      )

      val Some(result) = route(FakeRequest(POST, "/payment/make").withFormUrlEncodedBody(body:_*))
      status(result) must equalTo(OK)
      contentAsString(result) must contain("Transaction failure: ")
    }
  }
}
