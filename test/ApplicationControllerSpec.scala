import org.specs2.mutable._
import org.specs2.runner._
import org.junit.runner._
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
class ApplicationControllerSpec extends PlaySpecification {
  "QA Cinemas Application" should {

    "render the index page" in new WithApplication{
      route(FakeApplication(), FakeRequest(GET, "/")) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
        case _ => failure
      }
    }

    "render the about page" in new WithApplication{
      route(FakeApplication(), FakeRequest(GET, "/about")) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
        case _ => failure
      }
    }

    "render the contact page" in new WithApplication{
      route(FakeApplication(), FakeRequest(GET, "/contactus")) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
        case _ => failure
      }
    }

    "render the find page" in new WithApplication{
      route(FakeApplication(), FakeRequest(GET, "/findus")) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
        case _ => failure
      }
    }

    "render the certifications page" in new WithApplication{
      route(FakeApplication(), FakeRequest(GET, "/certifications")) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
        case _ => failure
      }
    }

    val listingsRoute = route(FakeApplication(), FakeRequest(GET, "/listings"))
    "should be able to retrieve the movies collection and render listings" in new WithApplication {
      listingsRoute match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
        case _ => failure
      }
    }

    "should be able to link to a route which contains a movie page" in new WithApplication {
      listingsRoute match {
        case Some(route) => contentAsString(route) must contain("<a href='movie/")
        case _ => failure
      }
    }

    "providing an invalid ID should return a bad request" in new WithApplication {
      route(FakeApplication(), FakeRequest(GET, "/movie/badid")) match {
        case Some(route) =>
          status(route) must equalTo(BAD_REQUEST)
          contentAsString(route) must contain("Invalid ID")
        case _ => failure
      }
    }

    "providing a valid but incorrect ID should return a bad request" in new WithApplication {
      //Non malformed ID
      val movieRoute = route(FakeApplication(), FakeRequest(GET, "/movie/deadbeefdeadbeefdeadbeef")) match {
        case Some(route) =>
          status(route) must equalTo(BAD_REQUEST)
          contentAsString(route) must contain("Movie ID does not exist.")
        case _ => failure
      }
    }
  }
}
