package util

import scala.collection.mutable.ListBuffer

/**
  * Created by Eric on 22/07/2017.
  */

object SeatGenerator {

  var seatHistory: ListBuffer[Int] = new ListBuffer()
  var sessionKeys: ListBuffer[String] = new ListBuffer()

  def getLayout(currentAccess: String): String = {
    val maxCol = 19
    val maxRow = 10
    val maxSeats = 201
    val html = "<br><table class=\"col-sm-12 seatsTable\"><tbody>"

    def helper(row: Int, col: Int, cur: Int)(html: String): String = col match{
      case _ if cur == maxSeats => html + "</tr></tbody></table>"
      case 0 =>  helper(row - 1, maxCol, cur + 1)(html + addButton(cur) + "</tr>")
      case `maxCol` => helper(row, col - 1, cur + 1)(html + "<tr>" + addButton(cur))
      case _ => helper(row, col - 1, cur + 1)(html + addButton(cur))
    }

    val res = helper(maxRow,maxCol,1)(html)
    res
  }

  private def addButton(seatNo: Int): String = {
    "<td><input type=button onclick=selectSeat(" + seatNo + ") " +
      "class=\"fsSubmitButton unavailable\" disabled  id=\"seat-" + seatNo + "\"></td>"
  }

  // json actions
  def isSeatAvailable(seat: Long): String ={
    "{\"available\": \"" + seatHistory.contains(seat) + " \"}"
  }

  def getSeats(client: String): String ={

    def getSeatsHelper(position: Long)(result: String): String= position match{
      case 0 => result.dropRight(1) + "]"
      case _ =>
        val posForAccess = seatHistory.indexOf(position)

        val bookedBy = (x: Int) => if(x>=0) (sessionKeys(x)==client).toString else "false"

        val newStr = "{\"seatid\":" + position + "," +
          "\"available\": \"false\", " +
          "\"bookedBy\": \"" + bookedBy(posForAccess) + "\"},"

        getSeatsHelper(position - 1)(result + newStr)
    }

    getSeatsHelper(200)("[")
  }

  def bookSeats(id: Int, client: String): String = seatHistory.contains(id) match{
    case true if sessionKeys(seatHistory.indexOf(id))==client=>
      seatHistory -= id
      sessionKeys -= client
      "{\"outcome\": \"success\",\"message\": \"seat unbooked\"}"
    case false =>
      seatHistory += id
      sessionKeys += client
      "{\"outcome\": \"success\",\"message\": \"seat booked\"}"
    case _ =>
      "{\"outcome\": \"failure\",\"message\": \"Seat already booked by someone else\"}"
  }

}
