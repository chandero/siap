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
import dto.QueryDto
@Singleton
class CobroController @Inject()(
    cobro6Service: Cobro6Repository,
    cobro2Service: Cobro2Repository,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )

  def siap_obtener_orden_trabajo_cobro_modernizacion = authenticatedUserAction.async { implicit request => 
      val empr_id = Utility.extraerEmpresa(request)
      val json = request.body.asJson.get
      // val page_size = (json \ "page_size").as[Long]
      // val current_page = (json \ "current_page").as[Long]
      val orderby = (json \ "orderby").as[String]
      val filter = (json \ "filter").as[QueryDto]
      val filtro_a = Utility.procesarFiltrado(filter)
      var filtro = filtro_a.replace("\"", "'")
      if (filtro == "()") {
        filtro = ""
      }
      cobro6Service.siap_obtener_orden_trabajo_cobro_modernizacion(empr_id.get, filtro, orderby).map { result =>
        Ok(write(result))
      }
      /*     
      reti_id match {
        case 6 => cobro6Service.siap_obtener_orden_trabajo_cobro_modernizacion(empr_id.get, 6).map { result =>
                  Ok(write(result))
                }
        case 2 => cobro2Service.siap_obtener_orden_trabajo_cobro_expansion(empr_id.get, 2).map { result =>
                  Ok(write(result))
                }
        case _ => Future.successful(Ok(write(List.empty)))
      }
      */
  }

  def siap_generar_orden_trabajo_cobro_modernizacion(
      anho: Int,
      mes: Int,
      tireuc_id: Int,
      reti_id: Long,
      cotr_consecutivo: Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    reti_id match {
      case 6 => cobro6Service
      .siap_generar_orden_trabajo_cobro_modernizacion(
        anho,
        mes,
        tireuc_id,
        reti_id,
        empr_id.get,
        cotr_consecutivo
      )
      .map { result =>
        Ok(write(result))
      }
      case 2 => cobro2Service.siap_generar_orden_trabajo_cobro_expansion(
        anho,
        mes,
        tireuc_id,
        reti_id,
        empr_id.get,
        cotr_consecutivo
      )
      .map { result =>
        Ok(write(result))
      }
    }
  }

  def siap_orden_trabajo_cobro(cotr_id: Long, reti_id: Long) = authenticatedUserAction.async { implicit request =>
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val (cotr_consecutivo, os) = reti_id match {
        case 6 => cobro6Service.siap_orden_trabajo_cobro(empr_id.get , cotr_id, usua_id.get)
        case 2 => cobro2Service.siap_orden_trabajo_cobro(empr_id.get , cotr_id, usua_id.get)
      }
      val filename = "Informe_Orden_Trabajo_ITAF_" + cotr_consecutivo + ".xlsx"
      val attach = "attachment; filename=" + filename
      Future.successful(Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
      )
  }

  def siap_orden_trabajo_cobro_verificar(reti_id: Long, anho: Int, mes: Int) = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val result = reti_id match {
      case 6 => cobro6Service.siap_orden_trabajo_verificar(empr_id.get, anho, mes)
      case 2 => cobro2Service.siap_orden_trabajo_verificar(empr_id.get, anho, mes)
    }
    Future.successful(Ok(write(result)))
  }

  def siap_orden_trabajo_cobro_consecutivo(reti_id: Long) = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val result = reti_id match {
      case 6 => cobro6Service.siap_orden_trabajo_cobro_consecutivo(empr_id.get)
      case 2 => cobro2Service.siap_orden_trabajo_cobro_consecutivo(empr_id.get)
    }
    Future.successful(Ok(write(result)))
  }

  def siap_orden_trabajo_cobro_relacion(anho:Int, periodo:Int) = authenticatedUserAction.async { implicit request =>
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val os = cobro6Service.siap_orden_trabajo_cobro_relacion(empr_id.get , anho, periodo)
      val filename = "Relacion_Orden_Trabajo_ITAF_" + anho.toString() + "_" + periodo.toString() + ".xlsx"
      val attach = "attachment; filename=" + filename
      Future.successful(Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
      )
  }
}