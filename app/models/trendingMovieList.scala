package models

import play.api.libs.json.{Json, OFormat}

/**
  * Created by Administrator on 03/08/2017.
  */
case class trendingMovieList(title: String)
object trendingMovieList{implicit val movieList :OFormat[trendingMovieList] = Json.format[trendingMovieList]}