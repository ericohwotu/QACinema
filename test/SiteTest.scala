import org.scalatest.selenium.HtmlUnit
import org.scalatest.{FlatSpec, ShouldMatchers}

class SiteTest extends FlatSpec with ShouldMatchers with HtmlUnit {

  val host = "http://localhost:9000"

  "Going to home page" should "have a title of homepage" in {
    go.to(host)
    pageTitle should be ("Homepage")
  }



}