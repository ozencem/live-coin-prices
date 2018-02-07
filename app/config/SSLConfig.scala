package config

import java.security.cert.X509Certificate
import javax.inject.{Inject, Singleton}
import javax.net.ssl.{SSLContext, X509TrustManager}

import akka.actor.ActorSystem
import akka.http.scaladsl.{ConnectionContext, Http, HttpsConnectionContext}
import play.Environment

@Singleton
class SSLConfig @Inject() (environment: Environment)(implicit actorSystem: ActorSystem) {

  if (environment.isDev) {
    // setup trustless ssl for dev environment
    val sslContext = {
      val context = SSLContext.getInstance("TLS")
      val trustManager = new X509TrustManager {
        override def checkServerTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}
        override def checkClientTrusted(x509Certificates: Array[X509Certificate], s: String): Unit = {}
        override def getAcceptedIssuers: Array[X509Certificate] = null
      }
      context.init(null, Array(trustManager), null)
      context
    }

    val httpsConnectionContext: HttpsConnectionContext = ConnectionContext.https(sslContext)
    Http().setDefaultClientHttpsContext(httpsConnectionContext)
  }
}
