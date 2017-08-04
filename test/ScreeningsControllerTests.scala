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

  "Application" should {

    "send 404 on a bad request" in new WithApplication{
      val result = route(FakeRequest(GET, "/boum")).get

      status(result) must equalTo(NOT_FOUND)
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("HomePage")
    }
  }

  "JsonApiController" should {

    "give bad request if no key is provided" in new WithApplication() {
      val getSeats = route(FakeApplication(),FakeRequest(GET,"/bookings/getseats")).get

      status(getSeats) must equalTo(UNAUTHORIZED)
    }

    "return a json api key" in new WithApplication() {
      val getApiKey = route(FakeApplication(), FakeRequest(GET, "/key/getkey")).get

      status(getApiKey) must equalTo(OK)
      contentAsJson(getApiKey).toString() must contain("blank")
    }
  }
}
