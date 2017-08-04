import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
import play.api.test._
import play.api.test.Helpers._

/**
 * Add your spec here.
 * You can mock out a whole application including requests, plugins etc.
 * For more information, consult the wiki.
 */
@RunWith(classOf[JUnitRunner])
class ApplicationControllerSpec extends PlaySpecification {
  "QA Cinemas Application" should {

    "render the index page" in new WithApplication{
      val home = route(FakeRequest(GET, "/")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
    }

    "render the about page" in new WithApplication{
      val home = route(FakeRequest(GET, "/about")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
    }

    "render the contact page" in new WithApplication{
      val home = route(FakeRequest(GET, "/contactUs")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
    }

    "render the find page" in new WithApplication{
      val home = route(FakeRequest(GET, "/findUs")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
    }

    "render the certifications page" in new WithApplication{
      val home = route(FakeRequest(GET, "/certifications")).get

      status(home) must equalTo(OK)
      contentType(home) must beSome.which(_ == "text/html")
    }

    val listingsRoute = route(FakeApplication(), FakeRequest(GET, "/listings")).get
    "should be able to retrieve the movies collection and render listings" in new WithApplication {
      status(listingsRoute) must equalTo(OK)
      contentType(listingsRoute) must beSome.which(_ == "text/html")
    }

    "should be able to link to a route which contains a movie page" in new WithApplication {
      contentAsString(listingsRoute) must contain("<a href='movie/")
    }

    "providing an invalid ID should return a bad request" in new WithApplication {
      val movieRoute = route(FakeApplication(), FakeRequest(GET, "/movie/badid")).get

      status(movieRoute) must equalTo(BAD_REQUEST)
      contentAsString(movieRoute) must contain("Invalid ID")
    }

    "providing a valid but incorrect ID should return a bad request" in new WithApplication {
      //Non malformed ID
      val movieRoute = route(FakeApplication(), FakeRequest(GET, "/movie/deadbeefdeadbeefdeadbeef")).get

      status(movieRoute) must equalTo(BAD_REQUEST)
      contentAsString(movieRoute) must contain("Movie ID does not exist.")
    }
  }
}
