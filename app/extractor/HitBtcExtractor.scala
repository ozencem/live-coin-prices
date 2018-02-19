package extractor

import akka.actor.ActorSystem
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.json.Json

import scala.concurrent.ExecutionContext

class HitBtcExtractor(val ticker: Ticker)(implicit actorSystem: ActorSystem,
                                          mat: Materializer,
                                          executionContext: ExecutionContext) extends WsExtractor {
  private val logger = Logger(getClass)

  override val tickerName: String = ticker.name

  override val exchangeName: String = "HitBTC"

  override val messagesToSendOnConnectionOpen: List[String] = List(Json.obj(
    "method" -> "subscribeTicker",
    "params" -> Json.obj("symbol" -> ticker.name.replace("/", ""),
    "id" -> 1))
    .toString())

  override val endpoint = "wss://api.hitbtc.com/api/2/ws"

  override val priceMapper: (String => String) = (msg: String) =>
    Json.parse(msg).\("params").\("last").asOpt[String].map(_.toDouble).map(p => f"$p%.8f").getOrElse("N/A")
}
