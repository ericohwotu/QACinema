import org.openqa.selenium.htmlunit.HtmlUnitDriver
import org.scalatest.selenium.HtmlUnit
import org.scalatest.{FlatSpec, ShouldMatchers}

class SiteTest extends FlatSpec with ShouldMatchers with HtmlUnit {

  val host = "http://localhost:9000"

  "The home page" should "load faster second time round due to caching" in {
    val startTime = System.currentTimeMillis()
    go.to(host)
    val firstLoad = System.currentTimeMillis() - startTime
    val startTime2 = System.currentTimeMillis()
    go.to(host)
    val secondLoad = System.currentTimeMillis() - startTime2
    assert(firstLoad > secondLoad)
  }

  "The listings page" should "load faster second time round due to caching" in {
    val startTime = System.currentTimeMillis()
    go.to(host + "/listings")
    val firstLoad = System.currentTimeMillis() - startTime
    val startTime2 = System.currentTimeMillis()
    go.to(host + "/listings")
    val secondLoad = System.currentTimeMillis() - startTime2
    assert(firstLoad > secondLoad)
  }

  "The listings page filtered by genre" should "load faster second time round due to caching" in {
    val startTime = System.currentTimeMillis()
    go.to(host + "/listings/action")
    val firstLoad = System.currentTimeMillis() - startTime
    val startTime2 = System.currentTimeMillis()
    go.to(host + "/listings/action")
    val secondLoad = System.currentTimeMillis() - startTime2
    assert(firstLoad > secondLoad)
  }

  "The search" should "load faster second time round due to caching" in {
    val startTime = System.currentTimeMillis()
    go.to(host + "/search/spider")
    val firstLoad = System.currentTimeMillis() - startTime
    val startTime2 = System.currentTimeMillis()
    go.to(host + "/search/spider")
    val secondLoad = System.currentTimeMillis() - startTime2
    assert(firstLoad > secondLoad)
  }

  "A movie page" should "load faster second time round due to caching" in {
    go.to(host + "/listings")
    val startTime = System.currentTimeMillis()
    click on xpath("/html/body/div/div[2]/div[1]/div[1]/a")
    val firstLoad = System.currentTimeMillis() - startTime
    go.to(host + "/listings")
    val startTime2 = System.currentTimeMillis()
    click on xpath("/html/body/div/div[2]/div[1]/div[1]/a")
    val secondLoad = System.currentTimeMillis() - startTime2
    assert(firstLoad > secondLoad)
  }

  "The admin page" should "load faster second time round due to caching" in {
    val startTime = System.currentTimeMillis()
    go.to(host + "/getallmovies")
    val firstLoad = System.currentTimeMillis() - startTime
    go.to(host)
    val startTime2 = System.currentTimeMillis()
    go.to(host + "/getallmovies")
    val secondLoad = System.currentTimeMillis() - startTime2
    assert(firstLoad > secondLoad)
  }

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