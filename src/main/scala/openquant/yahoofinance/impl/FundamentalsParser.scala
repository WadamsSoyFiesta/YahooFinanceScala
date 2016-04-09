package openquant.yahoofinance.impl

import java.time.format.DateTimeFormatter
import java.time.{LocalDate, ZoneId, ZonedDateTime}

import com.github.tototoshi.csv._
import openquant.yahoofinance.Fundamentals

import scala.io.Source

/**
  * Parses fundamental data in CSV format from Yahoo Finance into [[Fundamentals]]
  */
object FundamentalsParser extends Function1[String, Vector[Fundamentals]] {
  def apply(content: String): Vector[Fundamentals] = {
    val csvReader = CSVReader.open(Source.fromString(content))
    val fundamentals: Vector[Fundamentals] = csvReader.toStream.map { fields ⇒
      parseCSVLine(fields.toVector)
    }.toVector
    fundamentals
  }

  private def parseCSVLine(field: Vector[String]): Fundamentals = {
    require(field.length >= 2, "number of fields")
    val name = field(1)
    if (name == "N/A")
      Fundamentals(
        looksValid = false,
        symbol = field(0),
        name = name
      )
    else
      Fundamentals(
        looksValid = true,
        symbol = field(0),
        name = name
      )
  }
}