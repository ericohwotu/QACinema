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


}
