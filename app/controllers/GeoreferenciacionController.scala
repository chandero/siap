package controllers

import play.api.mvc.Request
import play.api.mvc.AnyContent
import play.api.Configuration

import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import models.GeoreferenciacionRepository
import play.api.mvc.ControllerComponents
import javax.inject.Inject
import play.api.mvc.AbstractController
import utilities.ImplicitJsonFormats
import utilities.DateTimeSerializer

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse
import net.liftweb.json.NoTypeHints
import net.liftweb.json.Serialization

@Singleton
class GeoreferenciacionController @Inject()(
    gService: GeoreferenciacionRepository,
    cc: ControllerComponents,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )

  def getGeoreferencia(
      t: Integer,
      barr_id: Int,
      aap_tecnologia: String,
      aap_potencia: Int,
      aap_id: Int
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    gService
      .getGeoreferencia(barr_id, aap_tecnologia, aap_potencia, aap_id)
      .map { georeferencia =>
        Ok(write(georeferencia))
      }
  }

  def getKey() = authenticatedUserAction {
    val apiKey: String = config.get[String]("google.apiKey")
    Ok(apiKey)
  }

  def getGeoreferenciaWeb(
      t: Integer,
      barr_id: Int,
      aap_tecnologia: String,
      aap_potencia: Int,
      aap_id: Int,
      token: String
  ) = { implicit request: Request[AnyContent] =>
    val secret = config.get[String]("play.http.secret.key")
    if (secret == token) {
      gService
        .getGeoreferencia(barr_id, aap_tecnologia, aap_potencia, aap_id)
        .map { georeferencia =>
          Ok(write(georeferencia))
        }
    } else {
      Ok(write("Token invalido"))
    }
  }

  def getKeyWeb(token: String) = {
    val secret = config.get[String]("play.http.secret.key")
    if (secret == token) {
      val apiKey: String = config.get[String]("google.apiKey")
      Ok(apiKey)
    } else {
      Ok(write("Token invalido"))
    }
  }
}
