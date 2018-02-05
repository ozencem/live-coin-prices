package extractor

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream.{ActorMaterializer, Materializer}
import akka.stream.scaladsl.{Flow, Keep, Sink, Source}
import play.api.Logger
import play.api.libs.json.{JsValue, Json}

import scala.concurrent.{ExecutionContext, Future}
import scala.util.control.NonFatal

trait Extractor{

  private val logger = Logger(getClass)

  def endpoint: String

  def priceMapper: String => JsValue

  def tickerName: String

  def start(wsSink: Sink[JsValue, _])(implicit actorSystem: ActorSystem,
                                      materializer: Materializer,
                                      executionContext: ExecutionContext) = {

    val outgoing = Source.maybe[Message]

    val incoming = Flow[Message].collect {
      case TextMessage.Strict(msg) => Future.successful(msg)
      case TextMessage.Streamed(stream) => stream
        .limit(100)
        .runFold("")(_ + _)
        .flatMap(msg => Future.successful(msg))
    }.mapAsync(4) (identity)
      .map(priceMapper)
      .map(p => Json.obj(tickerName -> p))
      .toMat(wsSink) (Keep.right)

    val wsFlow: Flow[Message, Message, _] = Flow.fromSinkAndSource(
      incoming,
      outgoing)

    createConnectionWithRetry(wsFlow)
  }

  def createWebsocketFlow()(implicit actorSystem: ActorSystem,
                            materializer: Materializer,
                            executionContext: ExecutionContext): Flow[Message, String, NotUsed] = {
    Flow[Message].collect {
      case TextMessage.Strict(msg) => Future.successful(msg)
      case TextMessage.Streamed(stream) => stream
        .limit(100)
        .runFold("")(_ + _)
        .flatMap(msg => Future.successful(msg))
    }.mapAsync(4) (identity(_))
  }

  def createConnectionWithRetry(wsFlow: Flow[Message, Message, _])(implicit actorSystem: ActorSystem,
                                                                   materializer: Materializer,
                                                                   executionContext: ExecutionContext): Unit = {
    try {
      val (upgradeResponse, promise) =
        Http().singleWebSocketRequest(
        WebSocketRequest(endpoint),
        wsFlow)

      upgradeResponse.map { upgrade =>
        if (upgrade.response.status == StatusCodes.SwitchingProtocols) {
          Done
        } else {
          logger.warn(s"Connection failed: ${upgrade.response.status}")
        }
      }

    } catch {
      case NonFatal(e) =>
        logger.warn("Could not create websocket connection!", e)
    }
  }
}
