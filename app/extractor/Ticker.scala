package extractor

sealed case class Ticker(name: String)

object Ticker {
  object ETHBTC extends Ticker("ETH/BTC")
  object XRPBTC extends Ticker("XRP/BTC")
  object VENBTC extends Ticker("VEN/BTC")
  object REQBTC extends Ticker("REQ/BTC")
  object TRXBTC extends Ticker("TRX/BTC")
  object NEOBTC extends Ticker("NEO/BTC")

  val values = Seq(ETHBTC, XRPBTC, VENBTC, REQBTC, TRXBTC, NEOBTC)
}