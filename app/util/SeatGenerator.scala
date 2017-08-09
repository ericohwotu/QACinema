package util

import scala.collection.mutable.ListBuffer

/**
  * Created by Eric on 22/07/2017.
  */

object SeatGenerator {

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

}
