package controllers

import javax.inject.Inject
import java.util.Base64
import java.util.Calendar

import models._
import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration

import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import scala.util.parsing.json._
import java.io.{OutputStream, ByteArrayOutputStream}
import org.joda.time.LocalDate
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import pdi.jwt.JwtSession

import utilities._

import dto._

import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.io.File

@Singleton
class InformeController @Inject()(
    informeService: InformeRepository,
    cc: ControllerComponents,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc) {

  def siap_detallado_material(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long
  ) = Action {
    val os = informeService
      .siap_detallado_material(fecha_inicial, fecha_final, usua_id, empr_id)
    Ok(os).as("application/pdf")
  }

  def siap_resumen_material_reporte(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long
  ) = Action {
    val os = informeService.siap_resumen_material_reporte(
      fecha_inicial,
      fecha_final,
      usua_id,
      empr_id
    )
    Ok(os).as("application/pdf")
  }

  def siap_resumen_material(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long
  ) = Action {
    val os = informeService
      .siap_resumen_material(fecha_inicial, fecha_final, usua_id, empr_id)
    Ok(os).as("application/pdf")
  }

  def siap_reporte_por_uso_xls(
      fecha_toma: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long
  ) = Action {
    val os =
      informeService.siap_reporte_por_uso_xls(fecha_toma, usua_id, empr_id)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Inventario_Por_Uso_" + fmt.print(fecha_toma) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Ok(os)
      .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      .withHeaders("Content-Disposition" -> attach)
  }

  def siap_graficos_reporte(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      usua_id: scala.Long,
      empr_id: scala.Long,
      num_id: Int
  ) = Action {
    val os = informeService.siap_graficos_reporte(
      fecha_inicial,
      fecha_final,
      usua_id,
      empr_id,
      num_id
    )
    Ok(os).as("application/pdf")
  }

  def siap_visita_por_barrio_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val usua_id = Utility.extraerUsuario(request)
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_visita_por_barrio_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_material_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val usua_id = Utility.extraerUsuario(request)
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_material_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_informe_detallado_material_muob_xls(muob_consecutivo: Int) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      informeService
        .siap_informe_detallado_material_muob_xls(muob_consecutivo, empr_id.get)
        .map { data =>
          Ok(Json.toJson(data))
        }
    }

  def siap_resumen_material_reporte_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val usua_id = Utility.extraerUsuario(request)
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_resumen_material_reporte_xls(
        fecha_inicial,
        fecha_final,
        empr_id.get
      )
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_cambio_direccion_xls(
      fecha_inicial: Long,
      fecha_final: Long,
      formato: String,
      token: String
  ) = Action { implicit request =>
    val secret = config.get[String]("play.http.secret.key")
    if (secret == token) {
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val os = informeService.siap_cambio_direccion_xls(
        fecha_inicial,
        fecha_final,
        empr_id.get,
        usua_id.get,
        formato
      )
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val dti = new DateTime(fecha_inicial)
      val dtf = new DateTime(fecha_final)
      var filename = "Siap_Informe_Cambio_Direccion_" + fmt
        .print(dti) + "_" + fmt.print(dtf)
      if (formato.equals("xls")) {
        filename += ".xlsx"
      } else {
        filename += ".pdf"
      }
      val attach = "attachment; filename=" + filename
      if (formato.equals("xls")) {
        Ok(os)
          .as(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          )
          .withHeaders("Content-Disposition" -> attach)
      } else {
        Ok(os)
          .as("application/pdf")
          .withHeaders("Content-Disposition" -> attach)
      }
    } else {
      NotFound
    }
  }

  def siap_resumen_material_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val usua_id = Utility.extraerUsuario(request)
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_resumen_material_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_expansion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_expansion_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_reubicacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_reubicacion_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_cambio_medida_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_cambio_medida_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_modernizacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_modernizacion_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_actualizacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_actualizacion_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_reposicion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_reposicion_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_repotenciacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_repotenciacion_xls(
        fecha_inicial,
        fecha_final,
        empr_id.get
      )
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_retiro_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_retiro_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_detallado_retiro_reubicacion_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_detallado_retiro_reubicacion_xls(
        fecha_inicial,
        fecha_final,
        empr_id.get
      )
      .map { data =>
        Ok(Json.toJson(data))
      }
  }
  def siap_reporte_consolidado_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_reporte_consolidado_xls(fecha_inicial, fecha_final, empr_id.get)
      .map { data =>
        Ok(Json.toJson(data))
      }
  }

  def siap_inventario(
      fecha_corte: scala.Long,
      page_size: scala.Long,
      current_page: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    val total = informeService.siap_inventario_total(fecha_corte, empr_id.get)
    informeService
      .siap_inventario(fecha_corte, empr_id.get, page_size, current_page)
      .map { data =>
        Ok(Json.obj("activos" -> data, "total" -> total))
      }
  }

  def siap_inventario_web(
      fecha_corte: scala.Long,
      empr_id: Long,
      token: String
  ) = Action.async { implicit request: Request[AnyContent] =>
    val secret = config.get[String]("play.http.secret.key")
    if (secret == token) {
      informeService.siap_inventario_web(fecha_corte, empr_id).map { data =>
        Ok(Json.toJson(data))
      }
    } else Future.successful(NotFound)
  }

  def siap_inventario_xls(fecha_corte: scala.Long, empr_id: Long) = Action {
    val os = informeService.siap_inventario_xls(fecha_corte, empr_id)
    val dt = new DateTime(fecha_corte)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Inventario_General_" + fmt.print(dt) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Ok(os)
      .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      .withHeaders("Content-Disposition" -> attach)
  }

  def siap_inventario_filtro_xls(
      fecha_corte: Long,
      orderby: String,
      filtrado: String,
      empr_id: Long
  ) = Action.async { implicit request: Request[AnyContent] =>
    val dt = new DateTime(fecha_corte)
    val order = (Base64.getDecoder().decode(orderby).map(_.toChar)).mkString
    println("orderby: " + order)
    val filters = (Base64.getDecoder().decode(filtrado).map(_.toChar)).mkString
    println("filtro: " + filters)
    val filter = Json.parse(filters).as[QueryDto]
    val filtro_a = Utility.procesarFiltrado(filter)
    var filtro = filtro_a.replace("\"", "'")
    if (filtro == "()") {
      filtro = ""
    }
    val os = informeService
      .siap_inventario_filtro_xls(fecha_corte, empr_id, order, filtro)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Inventario_Filtrado_" + fmt.print(dt) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Future.successful(
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    )
  }

  def siap_control_xls(fecha_corte: scala.Long, empr_id: Long) = Action {
    val os = informeService.siap_control_xls(fecha_corte, empr_id)
    val dt = new DateTime(fecha_corte)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Inventario_Control_" + fmt.print(dt) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Ok(os)
      .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      .withHeaders("Content-Disposition" -> attach)
  }

  def siap_control_filtro_xls(
      fecha_corte: Long,
      orderby: String,
      filtrado: String,
      empr_id: Long
  ) = Action.async { implicit request: Request[AnyContent] =>
    val dt = new DateTime(fecha_corte)
    val order = (Base64.getDecoder().decode(orderby).map(_.toChar)).mkString
    println("orderby: " + order)
    val filters = (Base64.getDecoder().decode(filtrado).map(_.toChar)).mkString
    println("filtro: " + filters)
    val filter = Json.parse(filters).as[QueryDto]
    val filtro_a = Utility.procesarFiltrado(filter)
    var filtro = filtro_a.replace("\"", "'")
    if (filtro == "()") {
      filtro = ""
    }
    val os = informeService
      .siap_control_filtro_xls(fecha_corte, empr_id, order, filtro)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Inventario_Control_Filtrado_" + fmt.print(dt) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Future.successful(
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    )
  }

  def siap_material_repetido_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      tiel_id: scala.Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    informeService
      .siap_material_repetido_xls(
        fecha_inicial,
        fecha_final,
        tiel_id,
        empr_id.get
      )
      .map { repetidos =>
        Ok(Json.toJson(repetidos))
      }
  }

  def siap_disponibilidad_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      wt: Double,
      empr_id: Long
  ) = Action {
    val os = informeService
      .siap_disponibilidad_xls(fecha_inicial, fecha_final, wt, empr_id)
    val fi = new DateTime(fecha_inicial)
    val ff = new DateTime(fecha_final)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Informe_Disponibilidad_" + fmt.print(fi) + "_" + fmt.print(
      ff
    ) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Ok(os)
      .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      .withHeaders("Content-Disposition" -> attach)
  }

  def siap_eficiencia_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: Long
  ) = Action {
    val os =
      informeService.siap_eficiencia_xls(fecha_inicial, fecha_final, empr_id)
    val fi = new DateTime(fecha_inicial)
    val ff = new DateTime(fecha_final)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Informe_Eficiencia_" + fmt.print(fi) + "_" + fmt
      .print(ff) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Ok(os)
      .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      .withHeaders("Content-Disposition" -> attach)
  }

  def siap_ucap_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: Long
  ) = Action {
    val os = informeService.siap_ucap_xls(fecha_inicial, fecha_final, empr_id)
    val fi = new DateTime(fecha_inicial)
    val ff = new DateTime(fecha_final)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Informe_Ucap_" + fmt.print(fi) + "_" + fmt
      .print(ff) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Ok(os)
      .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      .withHeaders("Content-Disposition" -> attach)
  }

  def siap_calculo_carga_xls(
      fecha_inicial: scala.Long,
      fecha_final: scala.Long,
      empr_id: scala.Long
  ) = Action {
    val os =
      informeService.siap_calculo_carga_xls(fecha_inicial, fecha_final, empr_id)
    val fi = new DateTime(fecha_inicial)
    val ff = new DateTime(fecha_final)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Informe_Calculo_Carga_" + fmt.print(fi) + "_" + fmt.print(
      ff
    ) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Ok(os)
      .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      .withHeaders("Content-Disposition" -> attach)
  }

  def siap_medidor_xls(empr_id: scala.Long, token: String) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
      val os = informeService.siap_medidor_xls(empr_id)
      val fi = new DateTime()
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val filename = "Informe_Medidores_" + fmt.print(fi) + ".xlsx"
      val attach = "attachment; filename=" + filename
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    } else {
      Forbidden("Dude, you’re not logged in.")
    }
  }

  def siap_transformador_xls = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val os = informeService.siap_transformador_xls(empr_id.get)
    val fi = new DateTime()
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "Informe_Transformadores_" + fmt.print(fi) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Future.successful(Ok(os)
      .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
      .withHeaders("Content-Disposition" -> attach)
    )
  }

  def siap_poste_xls(empr_id: scala.Long, token: String) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
      val os = informeService.siap_poste_xls(empr_id)
      val fi = new DateTime()
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val filename = "Informe_Postes_" + fmt.print(fi) + ".xlsx"
      val attach = "attachment; filename=" + filename
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    } else {
      Forbidden("Dude, you’re not logged in.")
    }
  }

  def siap_redes_xls(empr_id: scala.Long, token: String) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
      val os = informeService.siap_redes_xls(empr_id)
      val fi = new DateTime()
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val filename = "Informe_Redes_" + fmt.print(fi) + ".xlsx"
      val attach = "attachment; filename=" + filename
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    } else {
      Forbidden("Dude, you’re not logged in.")
    }
  }

  def siap_informe_solicitud_xls(fecha_inicial: Long, fecha_final: Long) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      var empr_id = Utility.extraerEmpresa(request)
      informeService
        .siap_informe_solicitud_xls(fecha_inicial, fecha_final, empr_id.get)
        .map { solicitudes =>
          Ok(Json.toJson(solicitudes))
        }
    }

  def siap_informe_solicitud_x_vencer_xls() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      var empr_id = Utility.extraerEmpresa(request)
      informeService.siap_informe_solicitud_x_vencer_xls(empr_id.get).map {
        solicitudes =>
          Ok(Json.toJson(solicitudes))
      }
  }

  def siap_informe_muot_xls(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: Long,
      token: String
  ) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
      val os = informeService
        .siap_informe_muot_xls(fecha_inicial, fecha_final, empr_id)
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val filename = "Informe_Muot_" + fmt.print(fecha_inicial) + "_a_" + fmt
        .print(fecha_final) + ".xlsx"
      val attach = "attachment; filename=" + filename
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    } else {
      Forbidden("Dude, you’re not logged in.")
    }
  }

  def siap_luminaria_por_reporte_xls(fecha_inicial: Long, fecha_final: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      var empr_id = Utility.extraerEmpresa(request)
      informeService.siap_luminaria_por_reporte_xls(fecha_inicial: Long, fecha_final: Long, empr_id.get).map {
        reportes =>
          Ok(Json.toJson(reportes))
      }
  }

  def siap_informe_por_cuadrilla_xls(fecha_inicial: Long, fecha_final: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      var empr_id = Utility.extraerEmpresa(request)
      informeService.siap_informe_por_cuadrilla_xls(fecha_inicial: Long, fecha_final: Long, empr_id.get).map {
        reportes =>
          Ok(Json.toJson(reportes))
      }
  }

  def siap_informe_obra_cuadrilla_xls(fecha_inicial: Long, fecha_final: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      var empr_id = Utility.extraerEmpresa(request)
      informeService.siap_informe_obra_cuadrilla_xls(fecha_inicial: Long, fecha_final: Long, empr_id.get).map {
        reportes =>
          Ok(Json.toJson(reportes))
      }
  }

  def siap_informe_general_operaciones_xls(      
      fecha_inicial: Long,
      fecha_final: Long,
      formato: String,
      empr_id: Long,
      token: String
  ) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
      val os = informeService.siap_informe_general_operaciones_xls(fecha_inicial, fecha_final, formato, empr_id)
      val fmt = DateTimeFormat.forPattern("yyyyMMdd")
      val filename = "Informe_General_Operaciones" + fmt.print(fecha_inicial) + "_a_" + fmt
        .print(fecha_final) + ".xlsx"
      val attach = "attachment; filename=" + filename
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    } else {
      Forbidden("Dude, you’re not logged in.")
    }
  }

  def siap_informe_general_estadistica_xls(      
      fecha_inicial: Long,
      fecha_final: Long,
      formato: String,
      empr_id: Long,
      token: String
  ) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
      val os = informeService.siap_informe_general_estadistica_xls(fecha_inicial, fecha_final, formato, empr_id)
      val fmt = DateTimeFormat.forPattern("yyyyMM")
      val filename = "Informe_General_Estadistico" + fmt.print(fecha_inicial) + "_a_" + fmt
        .print(fecha_final) + ".xlsx"
      val attach = "attachment; filename=" + filename
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    } else {
      Forbidden("Dude, you’re not logged in.")
    }
  }

  def siap_informe_resumen_aforo_xls(
      fecha_final: Long,
      empr_id: Long,
      token: String
  ) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
      val os = informeService.siap_informe_resumen_aforo_xls(fecha_final, empr_id)
      val fmt = DateTimeFormat.forPattern("yyyyMM")
      val filename = "Informe_Resumen_Aforo_" + fmt.print(fecha_final) + ".xlsx"
      val attach = "attachment; filename=" + filename
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    } else {
      Forbidden("Dude, you’re not logged in.")
    }
  } 

  def siap_informe_carga_aforo_xls(
    fecha_final: Long,
    empr_id: Long,
    token: String
  ) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
      val os = informeService.siap_informe_carga_aforo_xls(fecha_final, empr_id)
      val fmt = DateTimeFormat.forPattern("yyyyMM")
      val filename = "Informe_Carga_Aforo_" + fmt.print(fecha_final) + ".xlsx"
      val attach = "attachment; filename=" + filename
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    } else {
      Forbidden("Dude, you’re not logged in.")
    }    
  }

}
