package controllers

import com.braintreegateway
import com.braintreegateway.{BraintreeGateway, ClientTokenRequest, Transaction, TransactionRequest}
import play.api.mvc._

/**
  * Created by Administrator on 02/08/2017.
  */
class PaymentController extends Controller {
  val braintreeGateway : BraintreeGateway = new BraintreeGateway("access_token$sandbox$2ywzyb5rtfnk6m6h$10bfae954e82add885cac2188735ccda")
  val noAmount : Result = BadRequest("No amount provided.")

  def calculateDiscountedPrice(price : BigDecimal, pointsUsedString : String) : (BigDecimal, Long) = {
    val adjustedPrice = price - BigDecimal(pointsUsedString) / 1000
    if (0 > adjustedPrice) (BigDecimal(0), price.toLong * 1000) else (adjustedPrice, pointsUsedString.toLong)
  }

  def generateClientToken(): String = braintreeGateway.clientToken().generate(new ClientTokenRequest())
  def initiateClientToken() : Action[AnyContent] = Action { implicit request =>
    request.session.get("bookingPrice").fold(noAmount)(am => Ok(views.html.payment(generateClientToken(), am)))
  }

  def makeTransactionRequest(nonce : String, pointsUsed : String, request : Request[AnyContent]) : Result =
    request.session.get("bookingPrice").fold(noAmount) { am =>
      val discountUsage = calculateDiscountedPrice(BigDecimal(am.toString), pointsUsed)
      finalizeRequest(nonce, discountUsage._1, discountUsage._2, request)
    }

  def finalizeRequest(nonce: String, amount: BigDecimal, pointsUsed : Long, request : Request[AnyContent]) : Result = {
    val transactionRequest : TransactionRequest = new TransactionRequest()
    transactionRequest.amount(amount.bigDecimal).merchantAccountId("GBP").paymentMethodNonce(nonce)

    val result: braintreegateway.Result[Transaction] = braintreeGateway.transaction().sale(transactionRequest)
    if (result.isSuccess) {
      Ok(s"Transaction Success! Booking made!").withSession(request.session + ("pointsUsed" -> pointsUsed.toString))
    } else {
      Ok(s"Transaction failure:  ${result.getMessage}")
    }
  }

  def makePayment : Action[AnyContent] = Action { implicit request =>
    val body: Map[String, Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map())
    val nonce : Seq[String] = body.getOrElse("nonce", List[String]())
    val pointsUsed : Seq[String] = body.getOrElse("pointsUsed", List[String]("0"))

    nonce.headOption.fold(BadRequest("No nonce provided!"))(no =>
      makeTransactionRequest(no, pointsUsed.headOption.fold("0")(pu => pu), request)
    )
  }
}
