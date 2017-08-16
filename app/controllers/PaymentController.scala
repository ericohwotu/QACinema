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

  def generateClientToken(): String = braintreeGateway.clientToken().generate(new ClientTokenRequest())
  def initiateClientToken() : Action[AnyContent] = Action { implicit request =>
    request.session.get("bookingPrice").fold(noAmount)(am => Ok(views.html.payment(generateClientToken(), am)))
  }

  def makeTransactionRequest(nonce : String, request : Request[AnyContent]) : Result =
    request.session.get("bookingPrice").fold(noAmount)(am => finalizeRequest(nonce, BigDecimal(am.toString)))

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

  def makePayment : Action[AnyContent] = Action { implicit request =>
    val nonce : Seq[String] = request.body.asFormUrlEncoded.getOrElse(Map()).getOrElse("nonce", List[String]())

    nonce.headOption.fold(BadRequest("No nonce provided!"))(no => makeTransactionRequest(no, request))
  }
}
