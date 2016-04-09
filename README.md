# YahooFinanceScala
A non-blocking Yahoo Finance Scala client based on Akka streams.

[![Twitter Follow](https://img.shields.io/twitter/follow/openquantfin.svg?style=social)](https://twitter.com/intent/user?screen_name=openquantfin)
<span class="badge-paypal"><a
href="https://www.paypal.com/cgi-bin/webscr?cmd=_s-xclick&hosted_button_id=6SLKKT7NJUVM6"
title="Donate to this project using Paypal"><img
src="https://img.shields.io/badge/paypal-donate-yellow.svg" alt="PayPal donate button" /></a></span>


## Usage


`$ sbt console`

```scala
import scala.concurrent.duration.Duration
import openquant.yahoofinance.{YahooFinance, Quote, Fundamentals}
import akka.actor.ActorSystem
import java.time.ZonedDateTime
import scala.concurrent.Await

implicit val system = ActorSystem()

val yahooFinance = new YahooFinance()
val quotes: IndexedSeq[Quote] = Await.result(yahooFinance.quote("MSFT", ZonedDateTime.now().minusDays(5)), Duration.Inf)
// Quote(2016-04-01T00:00-04:00[America/New_York],55.049999,55.57,55.610001,54.57,24298600,55.57)
val fundamentals: IndexedSeq[Fundamentals] = Await.result(yahooFinance.fundamentals("IBM"), Duration.Inf)
// fundamentals: IndexedSeq[openquant.yahoofinance.Fundamentals] = Vector(Fundamentals(true,IBM,International Business Machines))
```
