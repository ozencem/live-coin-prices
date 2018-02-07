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
                                           executionContext: ExecutionContext) extends WsExtractor {
  private val logger = Logger(getClass)

  override val tickerName: String = ticker.name

  override val messagesToSendOnConnectionOpen: List[String] = List.empty

  override val endpoint = {
    val binanceTicker = ticker.name.toLowerCase.replace("/", "")
    s"${"wss://stream.binance.com:9443/ws/"}$binanceTicker@aggTrade"
  }

  override val priceMapper: (String => String) = (msg: String) => (Json.parse(msg) \ "p").asOpt[Double].map(_.toString).getOrElse("N/A")
}
