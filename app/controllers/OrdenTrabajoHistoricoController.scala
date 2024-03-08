package controllers

import javax.inject.Inject
import models.{OrdenTrabajoHistorico, OrdenTrabajoHistoricoRepository}
import play.api.mvc.{AbstractController, ControllerComponents}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import play.api.libs.json.JsNull

import org.joda.time.format.DateTimeFormat

import scala.concurrent.{ExecutionContext, Future}

import utilities._

class OrdenTrabajoHistoriaController @Inject()(
    ordenService: OrdenTrabajoHistoricoRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )


    def getOrden(id: Long)   = authenticatedUserAction.async {
        ordenService.getOrden(id).map { result =>
            Ok(write(result))
        }
    }

    def getOrders() = authenticatedUserAction.async {
        ordenService.getOrders().map { result =>
            Ok(write(result))
        }
    }
 

    def saveOrder = authenticatedUserAction.async { implicit request =>
        val jsonBody = request.body.asJson.getOrElse(JsNull)
        val ordenTrabajoHistorico = net.liftweb.json.parse((jsonBody).toString).extract[OrdenTrabajoHistorico]
        val empr_id = Utility.extraerEmpresa(request)
        val usua_id = Utility.extraerUsuario(request)

        ordenService.save(ordenTrabajoHistorico, empr_id.get, usua_id.get).map { result =>
            Ok(write(result))
        }
    }

    def getElementos = authenticatedUserAction.async { implicit request =>
        val empr_id = Utility.extraerEmpresa(request)
        val usua_id = Utility.extraerUsuario(request)
        ordenService.elementos(empr_id.get).map { result =>
            Ok(write(result))
        }
    }

    def getElementoByDescripcion(elhi_descripcion: String) = authenticatedUserAction.async { implicit request =>
        val empr_id = Utility.extraerEmpresa(request)
        val usua_id = Utility.extraerUsuario(request)
        ordenService.getElementoByDescripcion(elhi_descripcion, empr_id.get).map { result =>
            Ok(write(result))
        }
    }

    def getElementoByCodigo(elhi_codigo: String) = authenticatedUserAction.async { implicit request =>
        val empr_id = Utility.extraerEmpresa(request)
        val usua_id = Utility.extraerUsuario(request)
        ordenService.getElementoByCodigo(elhi_codigo, empr_id.get).map { result =>
            Ok(write(result))
        }
    }    
}