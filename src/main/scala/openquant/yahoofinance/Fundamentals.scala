package openquant.yahoofinance

case class Fundamentals(
  looksValid: Boolean,
  symbol: String,
  name: String = ""
)

