package models

import scala.collection.mutable.ListBuffer

/**
  * Created by Administrator on 01/08/2017.
  */
case class Movie(id:Int) {}

object Movie {
  val movies = ListBuffer[Movie](Movie(1), Movie(2), Movie(3), Movie(4), Movie(5))
}