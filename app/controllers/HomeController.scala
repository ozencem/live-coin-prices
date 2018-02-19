package controllers

import java.net.URI
import java.security.cert.X509Certificate
import javax.inject._
import javax.net.ssl.{SSLContext, TrustManager, X509TrustManager}

import akka.{Done, NotUsed}
import akka.actor.ActorSystem
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import akka.http.scaladsl.model.StatusCodes
import akka.http.scaladsl.model.ws.{Message, TextMessage, WebSocketRequest, WebSocketUpgradeResponse}
import akka.stream.{ActorMaterializer, Materializer, ThrottleMode}
import akka.stream.scaladsl.{BroadcastHub, Flow, Keep, MergeHub, Sink, Source}
import extractor.{BinanceExtractor, BittrexExtractor, HitBtcExtractor, Ticker}
import play.Environment
import play.api._
import play.api.libs.json.{JsObject, JsValue, Json}
import play.api.mvc._

import scala.concurrent.{ExecutionContext, Future, Promise}
import scala.util.Try
import scala.util.control.NonFatal
import scala.concurrent.duration.DurationInt

/**
 * This controller creates an `Action` to handle HTTP requests to the
 * application's home page.
 */
@Singleton
class HomeController @Inject()(cc: ControllerComponents,
                               environment: Environment,
                              config: Configuration)
                              (implicit actorSystem: ActorSystem,
                               materializer: Materializer,
                               executionContext: ExecutionContext,
                               webJarsUtil: org.webjars.play.WebJarsUtil)
                              extends AbstractController(cc) {

  private val logger = Logger(getClass)

  private val (wsSink, wsSource) = {
    val wsSink = BroadcastHub.sink[JsValue]
    val wsSource = MergeHub.source[JsValue]

    wsSource.toMat(wsSink)(Keep.both).run()
  }

  // TODO this is for debug
  // wsSource.runWith(Sink.foreach(println))
  Ticker.values.foreach(new BinanceExtractor(_).start(wsSink))
  Ticker.values.foreach(new BittrexExtractor(_).start(wsSink))
  Ticker.values.foreach(new HitBtcExtractor(_).start(wsSink))

  private val userFlow = {
    Flow.fromSinkAndSource(Sink.ignore, wsSource)
  }

  def index(): Action[AnyContent] = Action { implicit request: RequestHeader =>
    val webSocketUrl = routes.HomeController.socket().webSocketURL(secure = environment.isProd)
    Ok(views.html.index(webSocketUrl, tickers))
  }

  def socket(): WebSocket = {
    WebSocket.acceptOrResult[JsValue, JsValue] {
      case rh if sameOriginCheck(rh) =>
        Future.successful(userFlow).map { flow =>
          Right(flow)
        }.recover {
          case e: Exception =>
            val msg = "Cannot create websocket"
            logger.error(msg, e)
            val result = InternalServerError(msg)
            Left(result)
        }

      case rejected =>
        logger.error(s"Request ${rejected} failed same origin check")
        Future.successful {
          Left(Forbidden("forbidden"))
        }
    }
  }

  def tickers(): List[String] = {
    Ticker.values.map(_.name).toList
  }

  /**
    * Checks that the WebSocket comes from the same origin.  This is necessary to protect
    * against Cross-Site WebSocket Hijacking as WebSocket does not implement Same Origin Policy.
    *
    * See https://tools.ietf.org/html/rfc6455#section-1.3 and
    * http://blog.dewhurstsecurity.com/2013/08/30/security-testing-html5-websockets.html
    */
  private def sameOriginCheck(implicit rh: RequestHeader): Boolean = {
    // The Origin header is the domain the request originates from.
    // https://tools.ietf.org/html/rfc6454#section-7
    logger.debug("Checking the ORIGIN ")

    rh.headers.get("Origin") match {
      case Some(originValue) if originMatches(originValue) =>
        logger.debug(s"originCheck: originValue = $originValue")
        true

      case Some(badOrigin) =>
        logger.error(s"originCheck: rejecting request because Origin header value ${badOrigin} is not in the same origin")
        false

      case None =>
        logger.error("originCheck: rejecting request because no Origin header found")
        false
    }
  }

  /**
    * Returns true if the value of the Origin header contains an acceptable value.
    */
  private def originMatches(origin: String): Boolean = {
    val HttpPort = Try(System.getProperty("http.port").toInt).getOrElse(config.get[Int]("http.port"))
    val HttpsPort = Try(System.getProperty("https.port").toInt).getOrElse(config.get[Int]("https.port"))
    try {
      val url = new URI(origin)
      url.getHost == "localhost" || url.getHost == "live-coin-prices.herokuapp.com"
    } catch {
      case e: Exception => false
    }
  }
}
