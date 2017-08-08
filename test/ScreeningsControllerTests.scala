import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._

import scala.concurrent.Await
import scala.concurrent.duration.Duration


/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ScreeningsControllerTests extends Specification {

  val movieName = "Sample Booking"
  val apiKey = "8Nv6XI2hrq6zoqORrdRxzDbfDJY5W3AU"
  val seatId = 5

  "ScreeningsController" should {

    "render the index page" in new WithApplication{
      val home = route(FakeApplication(),FakeRequest(GET, "/bookings")).orNull

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("Sample Booking")
    }

    "send to payment screen" in new WithApplication() {
      val toPayment = route(FakeApplication(),FakeRequest(GET,"/bookings/topayment?amount=5")).orNull
      status(toPayment) must equalTo(SEE_OTHER)
    }

    "send to submit booking screen" in new WithApplication() {
      val toSubmit = route(FakeApplication(),FakeRequest(GET,"/bookings/confirm")).orNull
      status(toSubmit) must equalTo(SEE_OTHER)
    }
  }

  "ScreeningsApiController" should {

    "give bad request if no key is provided" in new WithApplication() {
      val getSeats = route(FakeApplication(),FakeRequest(GET,"/bookings/getseats")).orNull
      status(getSeats) must equalTo(BAD_REQUEST)
    }

    "return seats if all values provided" in new WithApplication() {
      FakeRequest(GET,"/bookings")
      val getSeats = route(FakeApplication(),FakeRequest(GET,"/bookings/getseats?" +
        "date=6%20AUG%202017&time=9:00").withSession(
        ("sessionKey","8Nv6XI2hrq6zoqORrdRxzDbfDJY5W3AU"),
        ("movieName","Created"))).orNull

      status(getSeats) must equalTo(OK)
    }

    "confirm bookings should redirect" in new WithApplication() {
      val getSeats = route(FakeApplication(),FakeRequest(GET,"/bookings/confirm")).orNull
      status(getSeats) must equalTo(SEE_OTHER)
    }

    "submit bookings should redirect" in new WithApplication() {
      val getSeats = route(FakeApplication(),FakeRequest(GET,"/bookings/submit?" +
        "key=8Nv6XI2hrq6zoqORrdRxzDbfDJY5W3AU&name=Sample%20Booking&date=6%20AUG%202017&" +
        "time=9:00").withSession(("loggedin","qacinema"),("bookingPrice","25"))).orNull

      status(getSeats) must equalTo(SEE_OTHER)
    }
  }

  "ScreenindsDbController" should {

    "return a json api key" in new WithApplication() {
      val getApiKey = route(FakeApplication(), FakeRequest(GET, "/key/getkey")).orNull

      status(getApiKey) must equalTo(OK)
      contentAsJson(getApiKey).toString() must contain("blank")
    }

    "return success on booking" in new WithApplication() {
      val bookSeat = route(FakeApplication(), FakeRequest(POST, "/bookings/bookseat?" +
        s"id=$seatId&date=7%20AUG%202017&time=9:00")
        .withSession(("sessionKey",apiKey),("movieName", movieName))).orNull

      val outcome = (contentAsJson(bookSeat) \ "outcome").as[String]

      status(bookSeat) must equalTo(OK)
      outcome must contain("success")
    }

    "return ok when unbook runner is started" in new WithApplication() {
      val unbookRunner = route(FakeApplication(), FakeRequest(GET, "/key/unbook")
        .withSession(("isTest","true"))).orNull
      println(Await.result(unbookRunner,Duration.Inf).header)
      status(unbookRunner) must equalTo(OK)
    }
  }
}
