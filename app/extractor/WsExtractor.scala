package extractor

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream.{ActorMaterializer, Materializer, OverflowStrategy, ThrottleMode}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait WsExtractor {

  private val logger = Logger(getClass)

  def endpoint: String

  def priceMapper: String => JsValue

  def tickerName: String

  def messagesToSendOnConnectionOpen: List[String]

  def start(wsSink: Sink[JsValue, _])(implicit actorSystem: ActorSystem,
                                      materializer: Materializer,
                                      executionContext: ExecutionContext) = {

    val outgoing = Source(messagesToSendOnConnectionOpen.map(TextMessage.Strict)).concat(Source.maybe)

    val incoming = Flow[Message].collect {
      case TextMessage.Strict(msg) => Future.successful(msg)
      case TextMessage.Streamed(stream) => stream
        .limit(100)
        .runFold("")(_ + _)
        .flatMap(msg => Future.successful(msg))
    }.mapAsync(4) (identity)
      .buffer(1, OverflowStrategy.dropHead)
      .throttle(1, 500 milliseconds, 1, ThrottleMode.Shaping)
      .map(priceMapper)
      .map(p => Json.obj(tickerName -> p))
      .toMat(wsSink) (Keep.right)

    val wsFlow: Flow[Message, Message, _] = Flow.fromSinkAndSource(
      incoming,
      outgoing)

    scheduleReconnection(wsFlow)
  }

  private def createConnectionWithRetry(wsFlow: Flow[Message, Message, _])(implicit actorSystem: ActorSystem,
                                                                   materializer: Materializer,
                                                                   executionContext: ExecutionContext): Unit = {
    try {
      val (upgradeResponse, promise) =
        Http().singleWebSocketRequest(
        WebSocketRequest(endpoint),
        Flow[Message].alsoTo(Sink.onComplete(_ => scheduleReconnection(wsFlow))).via(wsFlow))

      upgradeResponse.map { upgrade =>
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          Done
        } else {
          logger.warn(s"Connection failed: ${upgrade.response.status}")
          scheduleReconnection(wsFlow)
        }
      }

    } catch {
      case NonFatal(e) =>
        logger.warn("Could not create websocket connection!", e)
        scheduleReconnection(wsFlow)
    }
  }

  private def scheduleReconnection(wsFlow: Flow[Message, Message, _])(implicit actorSystem: ActorSystem,
                                                              materializer: Materializer,
                                                              executionContext: ExecutionContext): Unit = {
    logger.info("Scheduling connection attempt")
    actorSystem.scheduler.scheduleOnce(2 seconds)(createConnectionWithRetry(wsFlow))
  }
}
