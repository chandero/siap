package controllers

import javax.inject.Inject
import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration

import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import org.joda.time.DateTime

import pdi.jwt.JwtSession

import utilities._

@Singleton
class CobroController @Inject()(
    cobroService: CobroRepository,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )

  def siap_generar_orden_trabajo_cobro_modernizacion(
      anho: Int,
      mes: Int,
      tireuc_id: Int,
      reti_id: Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    cobroService
      .siap_generar_orden_trabajo_cobro_modernizacion(
        anho,
        mes,
        tireuc_id,
        reti_id,
        empr_id.get
      )
      .map { result =>
        Ok(write(result))
      }

  }

}
