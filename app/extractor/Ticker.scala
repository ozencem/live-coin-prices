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
  object VENBTC extends Ticker("VEN/BTC")
  object REQBTC extends Ticker("REQ/BTC")
  object WTCBTC extends Ticker("WTC/BTC")
  object KCSBTC extends Ticker("KCS/BTC")
  object ZRXBTC extends Ticker("ZRX/BTC")
  object TRXBTC extends Ticker("TRX/BTC")
  object ENGBTC extends Ticker("ENG/BTC")
  object FUNBTC extends Ticker("FUN/BTC")

  val values = Seq(ETHBTC, ADABTC, XRPBTC, LTCBTC, NEOBTC, XMRBTC, XLMBTC, VENBTC, REQBTC,
    WTCBTC, KCSBTC, ZRXBTC, TRXBTC, ENGBTC, FUNBTC)
}