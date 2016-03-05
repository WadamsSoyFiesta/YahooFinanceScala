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
    val res = Await.result(yahooFinance.quote("MSFT", ZonedDateTime.now().minusDays(5)), Duration.Inf)
    res.length must be_>=(3)
    res.length must be_<=(5)
  }
  "non-existent symbol" in {
    val yahooFinance = new YahooFinance()
    Await.result(yahooFinance.quote("qwertyasdf", ZonedDateTime.now().minusDays(5)), Duration.Inf) must throwA[RuntimeException]
  }
}