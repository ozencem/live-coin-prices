package extractor

sealed case class Ticker(name: String)

object Ticker {
  object ETHBTC extends Ticker("ETH/BTC")
  object BNBBTC extends Ticker("BNB/BTC")

  val values = Seq(ETHBTC, BNBBTC)
}