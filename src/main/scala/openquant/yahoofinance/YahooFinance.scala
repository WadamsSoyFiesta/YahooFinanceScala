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

import scala.collection.immutable.IndexedSeq
import scala.concurrent.Future

/**
  * Fetch quotes from Yahoo Finance asynchronously
  * @param actorSystem an akka ActorSystem to run on
  */
class YahooFinance(implicit actorSystem: ActorSystem, config: Config = ConfigFactory.load().getConfig("openquant.yahoofinance")) {
  implicit val materializer = ActorMaterializer()
  implicit val executionContext = actorSystem.dispatcher
  val Scheme = config.as[String]("scheme")
  val Host = config.as[String]("host")
  val Path = config.as[String]("path")

  private def handleResponse(response: HttpResponse): Future[Vector[Quote]] = {
    if (response.status.isFailure)
      Future.failed(new RuntimeException(response.status.reason))
    else {
      val yahooCSV = YahooCSV()
      val concat = Sink.fold[ByteString, ByteString](ByteString())(_ ++ _)
      val content = response.entity.dataBytes.runWith(concat).map(_.utf8String)
      val res = content.map { x ⇒
        yahooCSV.parse(x)
      }
      res
    }
  }

  /**
    * Get quotes from Yahoo Finance
    * @param symbol for example MSFT, IBM, etc.
    * @param from initial time
    * @param to end time
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

    val uri = Uri(scheme = Scheme).withQuery(query).withHost(Host).withPath(Uri.Path(Path))

    val request = HttpRequest(uri = uri)
    val res = Http().singleRequest(request)
    res.flatMap(handleResponse)
  }

}
