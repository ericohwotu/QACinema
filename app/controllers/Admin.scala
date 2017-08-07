package controllers

import javax.inject.Inject

import models.Movie
import play.api.libs.json.Json
import play.api.mvc.{Action, AnyContent, Controller}
import play.modules.reactivemongo.{MongoController, ReactiveMongoApi, ReactiveMongoComponents}
import reactivemongo.api.Cursor
import reactivemongo.bson.BSONDocument
import reactivemongo.play.json.collection.JSONCollection

import scala.concurrent.Future

/**
  * Created by Administrator on 07/08/2017.
  */
class Admin @Inject()(val reactiveMongoApi: ReactiveMongoApi) extends Controller
  with MongoController with ReactiveMongoComponents {

  def movieDBTable: Future[JSONCollection] = database.map(_.collection[JSONCollection]("movieDB"))
  def bookings: Future[JSONCollection] = database.map(_.collection[JSONCollection]("bookings"))

  def readByName(name: String): Action[AnyContent] = Action.async {
    val cursor: Future[Cursor[Movie]] = movieDBTable.map {
      _.find(Json.obj("Title" -> name))
        .cursor[Movie]
    }
    val futureUsersList: Future[List[Movie]] = cursor.flatMap(_.collect[List]())
    futureUsersList.map { persons => Ok(persons.headOption.get.Title)
    }
  }

  def create(newMovie: Movie): Action[AnyContent] = Action.async{
    val futureResult = movieDBTable.flatMap(_.insert(newMovie))
    futureResult.map(_ => Ok("Added movie " + newMovie.Title))
  }

  

//  def update(name: List[String]): Action[AnyContent] = Action.async {
//    val selector = BSONDocument("name" -> name.headOption)
//
//    val modifier = BSONDocument(
//      "$set" -> BSONDocument(
//        "name" -> name.headOption,
//        "description" -> name(1),
//        "maker" -> name(2),
//        "seller" -> name(3),
//        "image" -> name(4),
//        "price" -> name(5).toInt),
//      "$unset" -> BSONDocument("name" -> 1))
//
//    val futureResult = collection.flatMap(_.update(selector, modifier))
//    futureResult.map(_ => Ok("updated item " + name.headOption))

 // }

  def delete(name: String): Action[AnyContent] = Action.async {
    val selector = BSONDocument("name" -> name)
    val futureResult = movieDBTable.flatMap(_.remove(selector))
    futureResult.map(_ => Ok("Removed items with the name " + name))
  }


}
