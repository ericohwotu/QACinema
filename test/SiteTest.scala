import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.selenium.HtmlUnit
import org.scalatest.{FlatSpec, ShouldMatchers}

class SiteTest extends FlatSpec with ShouldMatchers with HtmlUnit {

  val host = "http://localhost:9000"
  "Starting from the main page you " should "be able to go navigate through the site to the booking page for a specific movie" in {
    go.to(host)
    click on linkText("Listings")
    pageTitle should be ("Listings")
    click on xpath("/html/body/div/div[2]/div[1]/div[1]/a/img")
    pageTitle should be("Movie")
    click on xpath("/html/body/div/div[1]/div/div/a/b/button")
    pageTitle should not be "Booking"
  }

  "Starting from the main page you " should "be able to go navigate to a movie carousel page" in {
    go.to(host)
    click on id("caro1")
    pageTitle should be("Movie")
  }

}