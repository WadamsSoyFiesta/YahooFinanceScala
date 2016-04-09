package openquant.yahoofinance

import java.time.ZonedDateTime
import java.time.temporal.ChronoField

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, Uri}
import akka.stream.ActorMaterializer
import akka.stream.scaladsl.Sink
import akka.util.ByteString
import com.typesafe.config.{Config, ConfigFactory}
import net.ceedubs.ficus.Ficus._
import openquant.yahoofinance.impl.{FundamentalsParser, QuoteParser}

import scala.collection.immutable.IndexedSeq
import scala.concurrent.Future

/**
  * Fetch quotes from Yahoo Finance asynchronously
  *
  * @param actorSystem an akka ActorSystem to run on
  */
class YahooFinance(implicit actorSystem: ActorSystem, config: Config = ConfigFactory.load().getConfig("openquant.yahoofinance")) {
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher
  val QuotesScheme = config.as[String]("quotes.scheme")
  val QuotesHost = config.as[String]("quotes.host")
  val QuotesPath = config.as[String]("quotes.path")

  val FundamentalsScheme = config.as[String]("fundamentals.scheme")
  val FundamentalsHost = config.as[String]("fundamentals.host")
  val FundamentalsPath = config.as[String]("fundamentals.path")

  /**
    * Get quotes from Yahoo Finance
    *
    * @param symbol     for example MSFT, IBM, etc.
    * @param from       initial time
    * @param to         end time
    * @param resolution quotes [[Resolution]], default is Day
    * @return future quotes or a failed future on error
    */
  def quotes(
    symbol: String,
    from: ZonedDateTime,
    to: ZonedDateTime = ZonedDateTime.now(),
    resolution: Resolution.Enum = Resolution.Day): Future[IndexedSeq[Quote]] = {
    // s: symbol
    // abc: from
    // def: to
    // g: resolution
    val params: Vector[(String, String)] = Vector(
      "s" → symbol,
      "a" → (from.get(ChronoField.MONTH_OF_YEAR) - 1).toString,
      "b" → from.get(ChronoField.DAY_OF_MONTH).toString,
      "c" → from.get(ChronoField.YEAR).toString,
      "d" → (to.get(ChronoField.MONTH_OF_YEAR) - 1).toString,
      "e" → to.get(ChronoField.DAY_OF_MONTH).toString,
      "f" → to.get(ChronoField.YEAR).toString,
      "g" → Resolution.parameter(resolution)
    )
    val query = Query(params: _*)

    val uri = Uri(scheme = QuotesScheme).withQuery(query).withHost(QuotesHost).withPath(Uri.Path(QuotesPath))

    val request = HttpRequest(uri = uri)
    val res = Http().singleRequest(request)
    res.flatMap(handleQuotesResponse)
  }

  private def handleQuotesResponse(response: HttpResponse): Future[Vector[Quote]] = {
    if (response.status.isFailure || response.status.isRedirection)
      Future.failed(new RuntimeException(response.status.reason))
    else {
      val parser = QuoteParser()
      val concat = Sink.fold[ByteString, ByteString](ByteString())(_ ++ _)
      val content: Future[String] = response.entity.dataBytes.runWith(concat).map(_.utf8String)
      val res = content.map { x ⇒
        parser.parse(x)
      }
      res
    }
  }

  def fundamentals(symbols: String*): Future[IndexedSeq[Fundamentals]] = {
    fundamentals(symbols.toIndexedSeq)
  }

  def fundamentals(symbols: IndexedSeq[String]): Future[IndexedSeq[Fundamentals]] = {
    val params: Vector[(String, String)] = Vector(
      "s" → symbols.mkString("+"),
      "f" → "sn"
    )
    val query = Query(params: _*)

    val uri = Uri(scheme = FundamentalsScheme).withQuery(query).withHost(FundamentalsHost).withPath(Uri.Path(FundamentalsPath))
    val request = HttpRequest(uri = uri)
    val res = Http().singleRequest(request)
    res.flatMap(handleFundamentalsResponse)
  }

  private def handleFundamentalsResponse(response: HttpResponse): Future[Vector[Fundamentals]] = {
    // FIXME: handle redirects
    if (response.status.isFailure || response.status.isRedirection)
      Future.failed(new RuntimeException(response.status.reason))
    else {
      val concat = Sink.fold[ByteString, ByteString](ByteString())(_ ++ _)
      val content: Future[String] = response.entity.dataBytes.runWith(concat).map(_.utf8String)
      val res = content.map(FundamentalsParser)
      res
    }
  }
}
