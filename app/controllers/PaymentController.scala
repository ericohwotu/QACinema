package controllers

import com.braintreegateway
import play.api._
import com.braintreegateway.{BraintreeGateway, Transaction, TransactionRequest}
import play.api.libs.json.JsLookupResult
import play.api.mvc.{Action, AnyContent}
import play.api.mvc._

/**
  * Created by Administrator on 02/08/2017.
  */
class PaymentController extends Controller {
  val braintreeGateway : BraintreeGateway = new BraintreeGateway("access_token$sandbox$2ywzyb5rtfnk6m6h$10bfae954e82add885cac2188735ccda")
  val amount = BigDecimal(10.00)

  def getClientToken : Action[AnyContent] = Action {
    Ok(views.html.payment(braintreeGateway.clientToken().generate(), amount.toString()))
  }

  def makeTransactionRequest(nonce : String) : Result = {
    val trReq : TransactionRequest = new TransactionRequest()
    trReq.amount(amount.bigDecimal).merchantAccountId("GBP").paymentMethodNonce(nonce)

    val result: braintreegateway.Result[Transaction] = braintreeGateway.transaction().sale(trReq);
    if (result.isSuccess) {
      Ok(s"Transaction Success! Booking made!")
    } else {
      Ok(s"Transaction failure:  ${result.getMessage}")
    }
  }

  def makePayment : Action[AnyContent] = Action { implicit request =>
    val paramval: String = request.body.asFormUrlEncoded.get("nonce").head
    if (paramval.nonEmpty) makeTransactionRequest(paramval)  else Ok("No nonce!")
  }

}
