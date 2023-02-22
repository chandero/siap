package controllers

import javax.inject.Inject
import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration
import play.filters.csrf._
import play.filters.csrf.CSRF.Token

import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import org.joda.time.DateTime

import pdi.jwt.JwtSession

import models.ActaRedimensionamientoRepository

import utilities._

import dto.QueryDto
import dto.ResultDto

@Singleton
class ActaRedimensionamientoController @Inject()(
    aService: ActaRedimensionamientoRepository,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )

        def getActasRedimensionamiento = authenticatedUserAction.async {
            implicit request: Request[AnyContent] =>
                val json = request.body.asJson.get
                val page_size = (json \ "page_size").as[Long]
                val current_page = (json \ "current_page").as[Long]
                val orderby = (json \ "orderby").as[String]
                val filter = (json \ "filter").as[QueryDto]
                val filtro_a = Utility.procesarFiltrado(filter)
                var filtro = filtro_a.replace("\"", "'")
                if (filtro == "()") {
                    filtro = ""
                }
                val empr_id = Utility.extraerEmpresa(request)
                val total = aService.cuenta(empr_id.get, filtro)
                aService
                    .getActasRedimensionamiento(empr_id.get, page_size, current_page, orderby, filtro)
                    .map { actas =>
                        Ok(write(new ResultDto[ActaRedimensionamiento](actas, total)))
                    }
        }

}