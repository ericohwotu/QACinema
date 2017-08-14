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
class ViewRecommendationsTest extends Specification {

  val movieName = "Sample Booking"
  val apiKey = "8Nv6XI2hrq6zoqORrdRxzDbfDJY5W3AU"
  val movieDate = "9%20AUG%202017"
  val username = "adam"
  val testMovie = "Test Movie"

  "Dashboard" should {
    "return a of recommended movies" in new WithApplication{
      val bookings = route(FakeApplication(),FakeRequest(GET, "/user/recommendations").withSession("loggedin"->"john")).orNull

      status(bookings) must equalTo(OK)
      contentAsString(bookings) must contain ("\"name\": \"Dunkirk\"")
    }
  }
}
