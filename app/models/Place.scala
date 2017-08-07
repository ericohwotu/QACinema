package models

import play.api.libs.json.{Json, OFormat}

/**
  * Created by Alex Rimmer on 03/08/2017.
  */
case class Place(name: String, latitude: String, longitude: String)

object Place { implicit val placeFormat :OFormat[Place] = Json.format[Place] }
