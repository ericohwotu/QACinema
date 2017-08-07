import javax.inject.Inject

import com.google.common.collect.ImmutableMap
import controllers.UserController
import models.Booking
import org.apache.http.annotation.Immutable
import org.junit.runner._
import org.specs2.mutable._
import org.specs2.runner._
import play.api.test.Helpers._
import play.api.test._
import play.api.libs.concurrent.Execution.Implicits.defaultContext

import scala.concurrent.Await
import scala.concurrent.duration.Duration

/**
  * Add your spec here.
  * You can mock out a whole application including requests, plugins etc.
  * For more information, consult the wiki.
  */
@RunWith(classOf[JUnitRunner])
class UserControllerTests extends Specification {


  "login" should {

    "should render the login page" in new WithApplication() {
      val login = route(FakeApplication(),FakeRequest(GET, "/login")).orNull
      status(login) must equalTo(OK)
    }

    "should return errors if username isnt present" in new WithApplication() {
      val login = route(FakeApplication(),FakeRequest(POST,"/login").withFormUrlEncodedBody(
        ("Username",""),("Password","testpassword")
      )).orNull

      status(login) must equalTo(BAD_REQUEST)
    }

    "should return errors if password isnt present" in new WithApplication() {
      val login = route(FakeApplication(),FakeRequest(POST,"/login").withFormUrlEncodedBody(
        ("Username","testuser"),("Password","")
      )).orNull

      status(login) must equalTo(BAD_REQUEST)
    }

    "should return errors if user doesnt exist" in new WithApplication() {
      val login = route(FakeApplication(),FakeRequest(POST,"/login").withFormUrlEncodedBody(
        ("Username","testuser"),("Password","testpassword")
      )).orNull

      status(login) must equalTo(BAD_REQUEST)
    }

    "should return successful if user and password are correct" in new WithApplication() {
      val login = route(FakeApplication(),FakeRequest(POST,"/login").withFormUrlEncodedBody(
        ("Username","qacinema"),("Password","qacinema123")
      )).orNull

      status(login) must equalTo(SEE_OTHER)
    }
  }

  "logout" should {

    "remove loggedin form session" in new WithApplication() {
      val login = route(FakeApplication(), FakeRequest(GET, "/logout")).orNull

      status(login) must equalTo(SEE_OTHER)
    }
  }

  "register" should {

    "register should render the page" in new WithApplication() {
      val register = route(FakeApplication(),FakeRequest(GET,"/register")).orNull

      status(register) must equalTo(OK)
    }

    "should return an error if no detail is provided" in new WithApplication() {
      val register = route(FakeApplication(),FakeRequest(POST,"/register").withFormUrlEncodedBody(
        ("Name",""),("Email",""),("Username",""),("Password",""),("Confirm Password","")
      )).orNull

      status(register) must equalTo(BAD_REQUEST)
    }

    "should redirect if all is good" in new WithApplication() {
      val register = route(FakeApplication(),FakeRequest(POST,"/register").withFormUrlEncodedBody(
        ("Name","Test"),("Email","Test@Main.com"),
        ("Username","test126"),("Password","password"),("Confirm Password","password")
      )).orNull

      status(register) must equalTo(SEE_OTHER)
    }
  }

  "Dashboard" should {
    "render the page if user is logged in" in new WithApplication() {
     FakeRequest(POST,"/login").withFormUrlEncodedBody(
        ("Username","qacinema"),("Password","qacinema123")
     )

      val dashboard = route(FakeApplication(),FakeRequest(GET,"/dashboard")).orNull
      status(dashboard) must equalTo(OK)
    }

    "redirect if no user logged" in new WithApplication() {
      FakeRequest(GET,"/logout")
      val dashboard = route(FakeApplication(),FakeRequest(GET,"/dashboard")).orNull
      status(dashboard) must equalTo(SEE_OTHER)
    }
  }

}
