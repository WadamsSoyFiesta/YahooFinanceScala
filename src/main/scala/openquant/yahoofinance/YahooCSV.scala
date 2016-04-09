package openquant.yahoofinance

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}

import com.github.tototoshi.csv._

import scala.io.Source

/**
  * Parses CSV response from Yahoo Finance into structured data
  */
class YahooCSV {
  private[this] val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private[this] val zoneId = ZoneId.of("America/New_York")
  private def parseDate(date: String): ZonedDateTime = {
    LocalDate.parse(date, df).atStartOfDay().atZone(zoneId)
  }
  private def parseCSVLine(field: Vector[String]): Quote = {
    require(field.length >= 7)
    Quote(
      parseDate(field(0)),
      BigDecimal(field(1)),
      BigDecimal(field(4)),
      BigDecimal(field(2)),
      BigDecimal(field(3)),
      BigDecimal(field(5)),
      BigDecimal(field(6))
    )
  }

  def parse(content: String): Vector[Quote] = {
    val csvReader = CSVReader.open(Source.fromString(content))
    val quotes = csvReader.toStream.drop(1).map { fields ⇒
      parseCSVLine(fields.toVector)
    }.toVector
    quotes
  }
}

object YahooCSV {
  def apply() = new YahooCSV
}
