package extractor

import akka.actor.ActorSystem
import akka.http.scaladsl.Http
import akka.http.scaladsl.model.Uri.Query
import akka.http.scaladsl.model.{HttpRequest, HttpResponse, StatusCodes, Uri}
import akka.stream.Materializer
import akka.stream.scaladsl.{Keep, Sink, Source}
import play.api.Logger
import play.api.libs.json.{JsNull, JsValue, Json}

import scala.concurrent.duration.DurationInt
import scala.concurrent.{ExecutionContext, Future}

trait HttpExtractor {

  private val logger = Logger(getClass)

  def endpoint: String

  def exchangeName: String

  def priceMapper: String => String

  def tickerName: String

  def start(wsSink: Sink[JsValue, _])(implicit actorSystem: ActorSystem,
                                      materializer: Materializer,
                                      executionContext: ExecutionContext) = {
    val source = Source.tick(1 seconds, 500 milliseconds, HttpRequest(uri = endpoint))
      .map(req => Http().singleRequest(req))
      source.mapAsync(4) (identity)
        .map {
          case HttpResponse(StatusCodes.OK, _, entity, _) =>
            entity.toStrict(100 milliseconds).map(_.data).flatMap(x => Future.successful(x.utf8String))
          case other =>
            logger.warn(s"http request failed, ${other}")
            Future.successful("")
        }
        .mapAsync(4) (identity)
          .alsoTo(Sink.foreach(println))
        .map(priceMapper)
        .statefulMapConcat { () =>
          var lastPrice = "N/A"
          price =>
            if (price == lastPrice) {
              List.empty
            } else {
              lastPrice = price
              List(price)
            }
        }
        .map(p => Json.obj(tickerName -> p))
        .toMat(wsSink) (Keep.right) .run()
  }

  def sendReq()(implicit actorSystem: ActorSystem,
                materializer: Materializer,
                executionContext: ExecutionContext): Future[HttpResponse] = {
    println("sending Request!!!!!!")
    val request = HttpRequest(uri = endpoint)
    Http().singleRequest(request)
  }
}
