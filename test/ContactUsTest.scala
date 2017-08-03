import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import org.specs2.matcher.Matchers
import play.api.test._
import play.api.test.Helpers._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ContactUsTest extends Specification {

  "Contact Controller" should {

    "send 404 on a bad request" in new WithApplication{
      val result = route(FakeApplication(),FakeRequest(GET, "/boum"))
      //val result = route(FakeRequest(GET, "/boum")).get
      status(result.get) must equalTo(NOT_FOUND)
    }

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/contactus")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
      contentAsString(home) must contain ("<form action=\"/contactus\" method=\"POST\" >")
    }

    "send error on empty form submission" in new WithApplication() {
      val result = route(FakeApplication(),FakeRequest(POST, "/contactus")).get
      status(result) must equalTo(BAD_REQUEST)
    }

    "send error on no name submitted" in new WithApplication() {
      val result = route(FakeApplication(),FakeRequest(POST, "/contactus?message=hello&email=me@you.com")).get
      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must contain("Name")
    }

    "send error on no email submitted" in new WithApplication() {
      val result = route(FakeApplication(),FakeRequest(POST, "/contactus?name=eric%20Ohwotu&message=hello")).get
      status(result) must equalTo(BAD_REQUEST)

      contentAsString(result) must contain("Email")
    }

    "send error on no message submitted" in new WithApplication() {
      val result = route(FakeApplication(),FakeRequest(POST, "/contactus?name=eric%20Ohwotu&email=me@you.com")).get
      status(result) must equalTo(BAD_REQUEST)
      contentAsString(result) must contain("Message")
    }

    "send 200 on success" in new WithApplication() {
      val result = route(FakeApplication(),FakeRequest(POST, "/contactus?Name=eric%20Ohwotu&Message=love%" +
        "20me%20or&Email=me@you.com&Subject=height-you")).get

      println(contentAsString(result))
      status(result) must equalTo(OK)
    }
  }
}
