package openquant.yahoofinance

object Resolution extends Enumeration {
  type Enum = Value
  val Day, Month, Week = Value

  def parameter(resolution: Enum): String = resolution match {
    case Day ⇒ "d"
    case Week ⇒ "w"
    case Month ⇒ "m"
  }
}


