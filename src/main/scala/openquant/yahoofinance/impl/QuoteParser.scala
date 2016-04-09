package openquant.yahoofinance.impl

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}

import com.github.tototoshi.csv._
import openquant.yahoofinance.Quote

import scala.io.Source

/**
  * Parses historical data in CSV format from Yahoo Finance into [[Quote]]
  */
class QuoteParser {
  private[this] val df = DateTimeFormatter.ofPattern("yyyy-MM-dd")
  private[this] val zoneId = ZoneId.of("America/New_York")

  def parse(content: String): Vector[Quote] = {
    val csvReader = CSVReader.open(Source.fromString(content))
    val quotes: Vector[Quote] = csvReader.toStream.drop(1).map { fields ⇒
      parseCSVLine(fields.toVector)
    }.toVector
    quotes
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

  private def parseDate(date: String): ZonedDateTime = {
    LocalDate.parse(date, df).atStartOfDay().atZone(zoneId)
  }
}

object QuoteParser {
  def apply() = new QuoteParser
}
