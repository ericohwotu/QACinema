import javax.inject.Inject

import controllers.Mongo

import scala.concurrent.{ExecutionContext, Future}
import org.scalatestplus.play._
import play.api.mvc._
import play.api.test._
import play.api.test.Helpers._
import play.modules.reactivemongo.ReactiveMongoApi
import reactivemongo.api.gridfs.GridFS
import reactivemongo.api.{DefaultDB, MongoConnection, MongoDriver}
import reactivemongo.play.json.JSONSerializationPack
import controllers.Mongo

import scala.concurrent.ExecutionContext.Implicits.global

class DatabaseTests @Inject()(reactiveMongoApi: ReactiveMongoApi) extends PlaySpec with Results{

  val controller = new Mongo(reactiveMongoApi)

  "getTrending #index" should {
    "should be valid" in {
      val trendingList = controller.getTrending
      val firstMovieName = trendingList.head.title
      firstMovieName mustBe "Spider-Man: Homecoming"
    }
  }

  "createFromApi #index" should {
    "should be valid" in new WithApplication(){
      val results = route(FakeApplication(),FakeRequest(GET,"/getMovie/Kidnap")).get
      contentAsString(results) mustBe "Kidnap"
    }
  }
}
