package extractor

import akka.actor.ActorSystem
import akka.{Done, NotUsed}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage}
import akka.stream.Materializer
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import extractor.Ticker
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future, Promise}

class BinanceExtractor(val ticker: Ticker)(implicit actorSystem: ActorSystem,
                                           mat: Materializer,
                                           executionContext: ExecutionContext) extends Extractor {
  private val logger = Logger(getClass)

  private val baseEndpoint: String = "wss://stream.binance.com:9443/ws/"

  override val tickerName: String = ticker.name

  override val endpoint = ticker match {
    case Ticker.ETHBTC => s"${baseEndpoint}ethbtc@trade"
    case Ticker.NEOBTC => s"${baseEndpoint}neobtc@trade"
    case Ticker.REQBTC => s"${baseEndpoint}reqbtc@trade"
    case Ticker.TRXBTC => s"${baseEndpoint}trxbtc@trade"
    case Ticker.VENBTC => s"${baseEndpoint}venbtc@trade"
    case Ticker.XRPBTC => s"${baseEndpoint}xrpbtc@trade"
    case ticker => throw new IllegalArgumentException(s"Endpoint for ${ticker.name} could not be found!")
  }

  override val priceMapper: (String => JsValue) = (msg: String) => (Json.parse(msg) \ "p").get
}
