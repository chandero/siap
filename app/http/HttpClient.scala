package http

import javax.inject.Inject

import requests._
import play.api.libs.ws._
import play.api.http.HttpEntity
import play.api.libs.json._
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}

import net.liftweb.json._
import net.liftweb.json.Serialization.write

import play.api.libs.ws.ahc.AhcWSResponse
import play.api.libs.ws.ahc.cache.CacheableHttpResponseStatus
import play.shaded.ahc.org.asynchttpclient.Response
import play.shaded.ahc.io.netty.handler.codec.http.DefaultHttpHeaders
import play.api.libs.ws.ahc.cache.CacheableHttpResponseBodyPart
import play.shaded.ahc.org.asynchttpclient.uri.Uri

class HttpClient @Inject()(
    ws: WSClient,
    conf: Configuration
)(implicit ec: ExecutionContext) {

  def sendSMS(destination: String, message: String): Future[Boolean] = {
    val url = conf.get[String]("sms.url")
    val correo = conf.get[String]("sms.correo")
    val password = conf.get[String]("sms.password")
    val data = Json.obj(
      "mensaje" -> message,
      "contactos" -> destination.split(",").toList
    )

    val authHeader = "Basic " + java.util.Base64.getEncoder
      .encodeToString(s"$correo:$password".getBytes)

    println("Enviando Mensaje A Proveedor SMS")

    ws.url(url)
      .addHttpHeaders(
        "Authorization" -> authHeader,
        "Content-Type" -> "application/json"
      )
      .post(data)
      .map { response =>
        println("Enviando provider response:" + response)
        if (response.status == 200) {
          val json: JsValue = Json.parse(response.body)
          if ( (json \ "enviados").as[Int] == 1) {
            true
          } else {
            false
          }
        } else {
          false
        }
      }
  }
}
