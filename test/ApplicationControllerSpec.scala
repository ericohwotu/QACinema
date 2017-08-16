import org.specs2.runner._
import org.junit.runner._
import play.api.test._

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
          contentAsString(route) must contain("<div id=\"map\"")
          contentAsString(route) must contain("<div id=\"rightPanel\"")
          contentAsString(route) must contain("<select id=\"cinemaMode\"")
          contentAsString(route) must contain("<title>Find Us</title>")
          contentAsString(route) must contain("input id=\"searchBar\"")
          contentAsString(route) must contain("<select id=\"travelMode\"")
          contentAsString(route) must contain("<select id=\"travelMode\"")
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
        case Some(route) => contentAsString(route) must contain("<a href='../movie/")
        case _ => failure
      }
    }

    "should be able to show movies by genre" in new WithApplication {
      route(FakeApplication(), FakeRequest(GET, "/listings/action")) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
        case _ => failure
      }
    }

    "should be able to search for movies via their title in a simple search" in new WithApplication {
      route(FakeApplication(), FakeRequest(GET, "/listings/title")) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
        case _ => failure
      }
    }

    "should be able to do a rich search with optional search parameters" in new WithApplication {
      val body : List[(String, String)] = List(
        ("Title", "dunkirk")
      )

      route(FakeApplication(), FakeRequest(POST, "/search").withFormUrlEncodedBody(body : _*)) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
          contentAsString(route) must contain("Movie Search")
          contentAsString(route) must not(contain("Please enter a query."))
          contentAsString(route) must contain("Title: dunkirk")
        case _ => failure
      }
    }

    "should not be able to do a rich search with invalid params" in new WithApplication {
      val body : List[(String, String)] = List(
        ("Title", ".x@ASDADA~")
      )

      route(FakeApplication(), FakeRequest(POST, "/search").withFormUrlEncodedBody(body : _*)) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
          contentAsString(route) must contain("Movie Search")
          contentAsString(route) must not(contain("Please enter a query."))
          contentAsString(route) must contain("An error occured in your search.")
        case _ => failure
      }
    }

    "should not allow a rich search with no params" in new WithApplication {
      route(FakeApplication(), FakeRequest(GET, "/search")) match {
        case Some(route) =>
          status(route) must equalTo(OK)
          contentType(route) must beSome.which(_ == "text/html")
          contentAsString(route) must contain("Movie Search")
          contentAsString(route) must contain("Please enter a query.")
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
          contentAsString(route) must contain("The requested movie ID does not exist...")
        case _ => failure
      }
    }
  }
}
