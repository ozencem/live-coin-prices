package extractor

sealed case class Ticker(name: String)

object Ticker {
  object ETHBTC extends Ticker("ETH/BTC")
  object XRPBTC extends Ticker("XRP/BTC")
  object LTCBTC extends Ticker("LTC/BTC")
  object NEOBTC extends Ticker("NEO/BTC")
  object BTGBTC extends Ticker("BTG/BTC")
  object ETCBTC extends Ticker("ETC/BTC")
  object XMRBTC extends Ticker("XMR/BTC")
  object BNTBTC extends Ticker("BNT/BTC")
  object ZECBTC extends Ticker("ZEC/BTC")
  object DASHBTC extends Ticker("DASH/BTC")
  object QTUMBTC extends Ticker("QTUM/BTC")
  object XVGBTC extends Ticker("XVG/BTC")
  object OMGBTC extends Ticker("OMG/BTC")
  object LSKBTC extends Ticker("LSK/BTC")
  object WAVESBTC extends Ticker("WAVES/BTC")

  val values = Seq(ETHBTC, XRPBTC, LTCBTC, NEOBTC, BTGBTC, ETCBTC, XMRBTC, BNTBTC, ZECBTC, DASHBTC, QTUMBTC, XVGBTC,
    OMGBTC, LSKBTC, WAVESBTC)
}