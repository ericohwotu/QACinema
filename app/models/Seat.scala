package models

import play.api.libs.json.{Json, OFormat}

/**
  * Created by Administrator on 03/08/2017.
  */
case class Seat(
                 id: Long,
                 author: String,
                 booked: Boolean,
                 expiry: Long,
                 kind: String
               )
object Seat{implicit val seats :OFormat[Seat] = Json.format[Seat]}