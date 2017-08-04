package controllers

import com.braintreegateway
import com.braintreegateway.{BraintreeGateway, ClientTokenRequest, Transaction, TransactionRequest}
import play.api.mvc.{Action, AnyContent}
import play.api.mvc._

/**
  * Created by Administrator on 02/08/2017.
  */
class PaymentController extends Controller {
  val braintreeGateway : BraintreeGateway = new BraintreeGateway("access_token$sandbox$2ywzyb5rtfnk6m6h$10bfae954e82add885cac2188735ccda")

  def generateClientToken(): String = braintreeGateway.clientToken().generate(new ClientTokenRequest())

  def getClientToken(amountOption : Option[Double]) : Action[AnyContent] = Action {
    amountOption match {
      case Some(amount) => Ok(views.html.payment(generateClientToken(), amount.toString))
      case None => BadRequest("No amount provided.")
    }
  }

  def makeTransactionRequest(nonce : String, amount : String) : Result = {
    val transactionRequest : TransactionRequest = new TransactionRequest()
    transactionRequest.amount(BigDecimal(amount).bigDecimal).merchantAccountId("GBP").paymentMethodNonce(nonce)

    val result: braintreegateway.Result[Transaction] = braintreeGateway.transaction().sale(transactionRequest)
    if (result.isSuccess) {
      Ok(s"Transaction Success! Booking made!")
    } else {
      Ok(s"Transaction failure:  ${result.getMessage}")
    }
  }

  def makePayment : Action[AnyContent] = Action { implicit request =>
    val urlencoded: Map[String, Seq[String]] = request.body.asFormUrlEncoded.getOrElse(Map())
    val nonce : Seq[String] = urlencoded.getOrElse("nonce", List[String]())
    val amount : Seq[String] = urlencoded.getOrElse("amount", List[String]())

    (nonce.headOption, amount.headOption) match {
      case (Some(no), Some(am)) => makeTransactionRequest(no, am)
      case _ => BadRequest("No nonce or no amount provided!")
    }
  }
}
