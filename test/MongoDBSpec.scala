import org.specs2.runner.{JUnitRunner, _}
import org.junit.runner._
import play.api.libs.json.Json
import play.api.mvc._
import play.api.test._
import play.api.libs.concurrent.Execution.Implicits._
import play.mvc.Http.RequestBuilder

import scala.concurrent.Await
import scala.concurrent.duration.Duration

@RunWith(classOf[JUnitRunner])
class MongoDBSpec extends PlaySpecification {

  "Adding Cinemas" should {
    "should return OK when adding locations" in new WithApplication() {
      route(FakeApplication(), FakeRequest(GET, "/addcinemas")) match {
        case Some(route) => status(route) must equalTo(OK)
        case _ => failure
      }
    }
  }

  "Adding Movies" should {
    "should return OK when adding movies" in new WithApplication() {
      route(FakeApplication(), FakeRequest(GET, "/addmovies")) match {
        case Some(route) => status(route) must equalTo(OK)
        case _ => failure
      }
    }
  }

  "Admin Page listing Movies" should {
    "should return OK listing movies" in new WithApplication() {
      route(FakeApplication(), FakeRequest(GET, "/getallmovies")) match {
        case Some(route) => status(route) must equalTo(OK)
        case _ => failure
      }
    }
  }

  "Admin Page creating a movie" should {
    "should return OK creating movies" in new WithApplication() {
      route(FakeApplication(), FakeRequest(POST, "/createmovie").withFormUrlEncodedBody(
        ("Title", "Test"),("Rated","Test"),("Released","Test"),("Runtime","Test"),("Genre","Test"),("Director","Test"),
        ("Actors","John Doe"),("Plot","test"),("Poster","N/A"),("video","N/A")
      )) match {
        case Some(route) => status(route) must equalTo(SEE_OTHER)
        case _ => failure
      }
    }
  }

  "Admin Page creating a movie" should {
    "should return BAD REQUEST creating movies" in new WithApplication() {
      route(FakeApplication(), FakeRequest(POST, "/createmovie").withFormUrlEncodedBody(
        ("Title", "Test"),("Rated","Test"),("Released","Test"),("Runtime","Test"),("Genre","Test"),("Director","Test"),
        ("Actors","John Doe"),("Plot",""),("Poster","N/A"),("video","N/A")
      )) match {
        case Some(route) => status(route) must equalTo(BAD_REQUEST)
        case _ => failure
      }
    }
  }


  "Admin Page updating a movie" should {
    "should return OK when updating movies" in new WithApplication() {
      route(FakeApplication(), FakeRequest(POST, "/update").withFormUrlEncodedBody(
        ("Title", "Test"),("Rated","Test"),("Released","Test"),("Runtime","Test"),("Genre","Test"),("Director","Test"),
        ("Actors","Updated Doe"),("Plot","test"),("Poster","N/A"),("video","N/A")
      )) match {
        case Some(route) => status(route) must equalTo(SEE_OTHER)
        case _ => failure
      }
    }
  }


  "Admin Page updating a movie" should {
    "should return BAD REQUEST when updating movies" in new WithApplication() {
      route(FakeApplication(), FakeRequest(POST, "/update").withFormUrlEncodedBody(
        ("Title", "Test"),("Rated","Test"),("Released","Test"),("Runtime","Test"),("Genre","Test"),("Director","Test"),
        ("Actors","Updated Doe"),("Plot",""),("Poster","N/A"),("video","N/A")
      )) match {
        case Some(route) => status(route) must equalTo(BAD_REQUEST)
        case _ => failure
      }
    }
  }


  "Admin Page deleting a movie" should {
    "should return OK listing movies" in new WithApplication() {
      route(FakeApplication(), FakeRequest(GET, "/delete/Test")) match {
        case Some(route) => status(route) must equalTo(SEE_OTHER)
        case _ => failure
      }
    }
  }


}
