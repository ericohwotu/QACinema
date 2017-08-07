import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ScreeningsControllerTests extends Specification {

  "ScreeningsController" should {

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/bookings")).orNull

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

  "ScreenindsDbController" should {

    "return a json api key" in new WithApplication() {
      val getApiKey = route(FakeApplication(), FakeRequest(GET, "/key/getkey")).orNull

      status(getApiKey) must equalTo(OK)
      contentAsJson(getApiKey).toString() must contain("blank")
    }

    "return success on booking" in new WithApplication() {
      val bookSeat = route(FakeApplication(), FakeRequest(POST, "/bookings/bookseat?" +
        "id=6&key=ZPhUEDqmCsWODx9G45xCyWisRNcUlqc5&name=Sample%20Booking&date=7%20AUG%202017&" +
        "time=9:00")).orNull

      val outcome = (contentAsJson(bookSeat) \ "outcome").as[String]

      status(bookSeat) must equalTo(OK)
      outcome must contain("success")
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
        ("sessionKey","ZPhUEDqmCsWODx9G45xCyWisRNcUlqc5"),
        ("movieName","Created"))).orNull

      status(getSeats) must equalTo(OK)
    }

    "submit bookings should redirect" in new WithApplication() {
      val getSeats = route(FakeApplication(),FakeRequest(GET,"/bookings/confirm")).orNull
      status(getSeats) must equalTo(SEE_OTHER)
    }

    "submit bookings should redirect" in new WithApplication() {
      val getSeats = route(FakeApplication(),FakeRequest(GET,"/bookings/submit?" +
        "key=ZPhUEDqmCsWODx9G45xCyWisRNcUlqc5&name=Sample%20Booking&date=6%20AUG%202017&" +
        "time=9:00").withSession(("loggedin","qacinema"),("bookingPrice","25"))).orNull

      status(getSeats) must equalTo(SEE_OTHER)
    }
  }
}
