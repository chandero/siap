package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import java.io.{OutputStream, ByteArrayOutputStream}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import org.joda.time.format.DateTimeFormat

import utilities._

import dto._

@Singleton
class OrdenTrabajoController @Inject()(
    ordenService: OrdenTrabajoRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )
  def todos(): Action[AnyContent] =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      val page_size = (json \ "page_size").as[Long]
      val current_page = (json \ "current_page").as[Long]
      val orderby = (json \ "orderby").as[String]
      val filter = (json \ "filter").as[QueryDto]
      var filtro = Utility.procesarFiltrado(filter)
      if (filtro == "()") {
        filtro = ""
      }
      val empr_id = Utility.extraerEmpresa(request)
      val total = ordenService.cuenta(empr_id.get)
      ordenService
        .todos(page_size, current_page, empr_id.get, orderby, filtro)
        .map { reportes =>
          Ok(Json.obj("ordenes" -> reportes, "total" -> total))
        }
    }

  def ordenes(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      ordenService.ordenes(empr_id.get).map { reportes =>
        Ok(Json.toJson(reportes))
      }
  }

  def getEstados(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      ordenService.estados().map { estados =>
        Ok(Json.toJson(estados))
      }
  }

  def buscarPorId(ortr_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val orden = ordenService.buscarPorId(ortr_id)
      orden match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(orden) => {
          Future.successful(Ok(Json.toJson(orden)))
        }
      }

  }

  def guardarOrden() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var orden = json.as[OrdenTrabajo]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val ordennuevo = new OrdenTrabajo(
        null,
        orden.ortr_fecha,
        orden.ortr_observacion,
        orden.ortr_consecutivo,
        orden.otes_id,
        orden.cuad_id,
        orden.cuad_descripcion,
        orden.tiba_id,
        empr_id,
        usua_id,
        orden.reportes,
        orden.obras,
        orden.novedades
      )
      ordenService.crear(ordennuevo, empr_id.get, usua_id.get).map { id =>
        if (id > 0) {
          Created(Json.toJson(id))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarOrden() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var orden = json.as[OrdenTrabajo]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val ordennuevo = new OrdenTrabajo(
        orden.ortr_id,
        orden.ortr_fecha,
        orden.ortr_observacion,
        orden.ortr_consecutivo,
        orden.otes_id,
        orden.cuad_id,
        orden.cuad_descripcion,
        orden.tiba_id,
        empr_id,
        usua_id,
        orden.reportes,
        orden.obras,
        orden.novedades
      )
      if (ordenService.actualizar(ordennuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarOrden(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (ordenService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }

  def imprimirOrden(id: Long, empr_id: Long) = Action {
    val os = ordenService.imprimir(id, empr_id)
    Ok(os).as("application/pdf")
  }

  def agregarReporte(ortr_id: Long, repo_id: Long, tireuc_id: Int) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (ordenService.agregarReporte(ortr_id, repo_id, tireuc_id)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
    }

  def agregarObra(ortr_id: Long, obra_id: Long) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (ordenService.agregarObra(ortr_id, obra_id)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
    }

  def obtenerReporteObraPorCuadrilla(cuad_id: Long) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val reportes = ordenService.obtenerReporteObraPorCuadrilla(cuad_id)
      Future.successful(Ok(write(reportes)))
    }

  def obtenerReporteObraPorCuadrillaMobile(cuad_id: Long) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      ordenService.obtenerReporteObraPorCuadrillaMobile(cuad_id).map {
        reportes =>
          println("Reportes: " + reportes)
          Ok(write(reportes))
      }
    }

  def siap_informe_cambios_en_reporte(fecha_inicial: scala.Long, fecha_final: scala.Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val os = ordenService.siap_informe_cambios_en_reporte(fecha_inicial, fecha_final, empr_id.get)
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val filename = "Informe_Cambios_En_Reportes_" + fmt.print(fecha_inicial) + "_" + fmt.print(fecha_final) + ".xlsx"
      val attach = "attachment; filename=" + filename
          Future.successful(Ok(os)
            .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
            .withHeaders("Content-Disposition" -> attach))

  }
}
