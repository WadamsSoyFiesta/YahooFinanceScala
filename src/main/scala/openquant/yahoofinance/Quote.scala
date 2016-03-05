package openquant.yahoofinance

import java.time.ZonedDateTime

case class Quote(
  date: ZonedDateTime,
  open: BigDecimal,
  close: BigDecimal,
  high: BigDecimal,
  low: BigDecimal,
  volume: BigDecimal,
  adjClose: BigDecimal
)
