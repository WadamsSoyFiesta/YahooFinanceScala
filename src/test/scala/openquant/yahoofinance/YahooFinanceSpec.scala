package openquant.yahoofinance

import java.time.ZonedDateTime

import akka.actor.ActorSystem
import akka.testkit.TestKit
import org.specs2.matcher.{FutureMatchers, Matchers}
import org.specs2.mutable._

import scala.concurrent.Await
import scala.concurrent.duration.Duration

class YahooFinanceSpec extends TestKit(ActorSystem()) with SpecificationLike with Matchers with Logging {
  "get quotes" in {
    val yahooFinance = new YahooFinance()
    val res = Await.result(yahooFinance.quotes("MSFT", ZonedDateTime.now().minusDays(5)), Duration.Inf)
    res.length must be_>=(3)
    res.length must be_<=(5)
  }
  "non-existent symbol" in {
    val yahooFinance = new YahooFinance()
    Await.result(yahooFinance.quotes("qwertyasdf", ZonedDateTime.now().minusDays(5)), Duration.Inf) must throwA[RuntimeException]
  }
  "invalid fundamentals" in {
    val yahooFinance = new YahooFinance()
    val invalids = Await.result(yahooFinance.fundamentals(Vector("qwertyasdf")), Duration.Inf)
    invalids must have size (1)
    invalids.head.looksValid must beFalse
  }

  "valid fundamentals" in {
    val yahooFinance = new YahooFinance()
    val syms = Vector("MSFT", "IBM")
    val valids = Await.result(yahooFinance.fundamentals(syms), Duration.Inf)
    valids must have size(2)
    valids.foreach { x ⇒
      x.looksValid must beTrue
      x.name must not beEmpty
    }
    valids.map { _.symbol } must contain(exactly(syms:_*))
    ok
  }
}