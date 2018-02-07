package extractor

sealed case class Ticker(name: String)

object Ticker {
  object ETHBTC extends Ticker("ETH/BTC")
  object ADABTC extends Ticker("ADA/BTC")
  object XRPBTC extends Ticker("XRP/BTC")
  object LTCBTC extends Ticker("LTC/BTC")
  object NEOBTC extends Ticker("NEO/BTC")
  object XMRBTC extends Ticker("XMR/BTC")
  object XLMBTC extends Ticker("XLM/BTC")
  object DASHBTC extends Ticker("DASH/BTC")
  object REQBTC extends Ticker("REQ/BTC")
  object OMGBTC extends Ticker("OMG/BTC")
  object LSKBTC extends Ticker("LSK/BTC")
  object WAVESBTC extends Ticker("WAVES/BTC")
  object ENGBTC extends Ticker("ENG/BTC")
  object FUNBTC extends Ticker("FUN/BTC")

  val values = Seq(ETHBTC, ADABTC, XRPBTC, LTCBTC, NEOBTC, XMRBTC, XLMBTC, DASHBTC, REQBTC,
    OMGBTC, LSKBTC, WAVESBTC, ENGBTC, FUNBTC)
}