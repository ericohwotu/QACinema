package controllers

import javax.inject.Inject

import models.Place
import play.api.cache.{CacheApi, NamedCache}
import play.api.libs.concurrent.Execution.Implicits.defaultContext
import play.api.libs.json.Json
import play.api.mvc.Controller
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.play.json.collection.JSONCollection
import reactivemongo.play.json._

import scala.concurrent.Future

/**
  * Created by Administrator on 08/08/2017.
  */
class LocationController @Inject() (@NamedCache("document-cache") cached: CacheApi,
                                    val reactiveMongoApi: ReactiveMongoApi)
  extends Controller with MongoController with ReactiveMongoComponents {

  def locationCollection: Future[JSONCollection] = cached.getOrElse[Future[JSONCollection]]("locationCollection") {
    database.map(_.collection[JSONCollection]("locations"))
  }
  def cinemasList: Future[List[Place]] = locationCollection.map {
    _.find(Json.obj()).cursor[Place]
  }.flatMap(_.collect[List]())
}
