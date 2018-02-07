package extractor

import akka.actor.ActorSystem
import akka.http.scaladsl.model.Uri
import akka.stream.Materializer
import play.api.Logger
import play.api.libs.json.{JsString, JsValue, Json}

import scala.concurrent.ExecutionContext

class BittrexExtractor(val ticker: Ticker)(implicit actorSystem: ActorSystem,
                                           mat: Materializer,
                                           executionContext: ExecutionContext) extends HttpExtractor  {
  private val logger = Logger(getClass)

  override val tickerName: String = ticker.name

  override val exchangeName: String = "Bittrex"

  override val endpoint: String = "https://bittrex.com/api/v1.1/public/getticker?market=" + tickerName.split("/").reverse.mkString("-")

  override val priceMapper: (String => String) = (msg: String) => (Json.parse(msg) \ "result" \ "Last").asOpt[Double].map(p => f"$p%.8f").getOrElse("N/A")
}
