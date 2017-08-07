package controllers

import com.braintreegateway
import com.braintreegateway.{BraintreeGateway, ClientTokenRequest, Transaction, TransactionRequest}
import play.api.mvc._

/**
  * Created by Administrator on 02/08/2017.
  */
class PaymentController extends Controller {
  val braintreeGateway : BraintreeGateway = new BraintreeGateway("access_token$sandbox$2ywzyb5rtfnk6m6h$10bfae954e82add885cac2188735ccda")

  def generateClientToken(): String = braintreeGateway.clientToken().generate(new ClientTokenRequest())

  def getClientToken() : Action[AnyContent] = Action { implicit request =>
    request.session.get("bookingPrice") match {
      case Some(amount) => Ok(views.html.payment(generateClientToken(), amount))
      case None => BadRequest("No amount provided.")
    }
  }

  def finalizeRequest(nonce: String, amount: BigDecimal) : Result = {
    val transactionRequest : TransactionRequest = new TransactionRequest()
    transactionRequest.amount(amount.bigDecimal).merchantAccountId("GBP").paymentMethodNonce(nonce)

    val result: braintreegateway.Result[Transaction] = braintreeGateway.transaction().sale(transactionRequest)
    if (result.isSuccess) {
      Ok(s"Transaction Success! Booking made!")
    } else {
      Ok(s"Transaction failure:  ${result.getMessage}")
    }
  }

  def makeTransactionRequest(nonce : String, request : Request[AnyContent]) : Result = {
    request.session.get("bookingPrice") match {
      case Some(amount) => finalizeRequest(nonce, BigDecimal(amount.toString))
      case None => BadRequest("No amount provided.")
    }
  }

  def makePayment : Action[AnyContent] = Action { implicit request =>
    val urlencoded: Map[String, Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map())
    val nonce : Seq[String] = urlencoded.getOrElse("nonce", List[String]())

    nonce.headOption match {
      case Some(no) => makeTransactionRequest(no, request)
      case _ => BadRequest("No nonce provided!")
    }
  }
}
