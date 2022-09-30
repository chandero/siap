package controllers

import javax.inject.Inject
import java.io.{OutputStream, ByteArrayOutputStream}
import java.nio.file.{Files, Path, Paths}
import java.util.Base64

import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration
import play.filters.csrf._
import play.filters.csrf.CSRF.Token

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Await, Future}
import scala.concurrent.duration._
import scala.util.{Try, Success, Failure}

import org.joda.time.LocalDate
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import pdi.jwt.JwtSession

import utilities._

import dto._
import java.text.SimpleDateFormat

@Singleton
class ReporteController @Inject()(
    reporteService: ReporteRepository,
    cc: ControllerComponents,
    config: Configuration,
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
      val total = reporteService.cuenta(empr_id.get)
      reporteService
        .todos(page_size, current_page, empr_id.get, orderby, filtro)
        .map { reportes =>
          Ok(write(new ReporteResult(reportes, total)))
        }
    }

  def reportes(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      reporteService.reportes(empr_id.get).map { reportes =>
        Ok(write(reportes))
      }
  }

  def estados(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      reporteService.estados().map { estados =>
        Ok(Json.toJson(estados))
      }
  }

  def tipos(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      reporteService.tipos().map { tipos =>
        Ok(Json.toJson(tipos))
      }
  }

  def buscarPorId(repo_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val reporte = reporteService.buscarPorId(repo_id)
      reporte match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(reporte) => {
          Future.successful(Ok(write(reporte)))
        }
      }
  }

  def buscarPorConsecutivo(reti_id: Long, repo_consecutivo: Int) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      println("por consecutivo")
      val empr_id = Utility.extraerEmpresa(request)
      val reporte = reporteService.buscarPorConsecutivo(
        reti_id,
        repo_consecutivo,
        empr_id.get
      )
      reporte match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(reporte) => {
          Future.successful(Ok(write(reporte)))
        }
      }
    }

  def buscarPorConsecutivoMovil(reti_id: Long, repo_consecutivo: Int) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      println("por consecutivo")
      val empr_id = Utility.extraerEmpresa(request)
      val reporte = reporteService.buscarPorConsecutivoMovil(
        reti_id,
        repo_consecutivo,
        empr_id.get
      )
      reporte match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(reporte) => {
          Future.successful(Ok(write(reporte)))
        }
      }
    }

  def buscarPorRango(anho: Int, mes: Int, tireuc_id: Int) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      reporteService.buscarPorRango(anho, mes, tireuc_id, empr_id.get).map {
        reportes =>
          Ok(write(reportes))
      }
    }

  def buscarPorAap(aap_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      reporteService.reportesporaap(aap_id, empr_id.get).map { reportes =>
        Ok(write(reportes))
      }
  }

  def guardarReporte() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var reporte = net.liftweb.json.parse(json.toString).extract[Reporte]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val reportenuevo = new Reporte(
        null,
        reporte.tireuc_id,
        reporte.reti_id,
        reporte.repo_consecutivo,
        reporte.repo_fecharecepcion,
        reporte.repo_direccion,
        reporte.repo_nombre,
        reporte.repo_telefono,
        reporte.repo_fechasolucion,
        reporte.repo_horainicio,
        reporte.repo_horafin,
        reporte.repo_reportetecnico,
        reporte.repo_descripcion,
        reporte.repo_subrepoconsecutivo,
        reporte.rees_id,
        reporte.orig_id,
        reporte.barr_id,
        empr_id,
        reporte.tiba_id,
        usua_id,
        reporte.adicional,
        reporte.meams,
        reporte.eventos,
        reporte.direcciones,
        reporte.novedades
      )
      reporteService.crear(reportenuevo).map {
        case (id, consec) =>
          if (id > 0) {
            Created(Json.obj("id" -> id, "consec" -> consec))
          } else {
            NotAcceptable(Json.toJson("true"))
          }
      }
  }

  def guardarReporteWeb() = Action.async {
    implicit request: Request[AnyContent] =>
      implicit val dateTimeJsReader =
        JodaReads.jodaDateReads("yyyy-MM-dd HH:mm:ss")
      val json = request.body.asJson.get
      val repoweb = json.as[ReporteWebDto]
      println("Json: " + json)
      val secret = config.get[String]("play.http.secret.key")
      val token = repoweb.token match {
        case Some(t) => t
        case None    => "error"
      }
      if (secret == token) {
        val repo_direccion = repoweb.repo_direccion
        val barr_id = repoweb.barr_id
        val repo_nombre = repoweb.repo_nombre
        val repo_telefono = repoweb.repo_telefono
        val repo_email = repoweb.repo_email
        val repo_descripcion = repoweb.repo_descripcion
        val repo_fecharecepcion = repoweb.repo_fecharecepcion match {
          case Some(d) =>
            DateTime.parse(d, DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss"))
          case None => DateTime.now
        }
        var repo_fechadigitacion = new DateTime()
        val acti_id = repoweb.acti_id
        val empr_id = repoweb.empr_id
        var ra = new ReporteAdicional(
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          repo_email,
          acti_id,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None,
          None
        )
        val usua_id = 1
        val reportenuevo = new Reporte(
          null,
          Some(1),
          Some(1),
          None,
          Some(repo_fecharecepcion),
          repo_direccion,
          repo_nombre,
          repo_telefono,
          None,
          None,
          None,
          None,
          repo_descripcion,
          None,
          Some(1),
          Some(9),
          barr_id,
          empr_id,
          None,
          Some(usua_id),
          Some(ra),
          None,
          None,
          None,
          None
        )
        reporteService.crear(reportenuevo).map {
          case (id, consec) =>
            if (id > 0) {
              Created(Json.obj("id" -> id, "consec" -> consec))
            } else {
              NotAcceptable(Json.toJson("error"))
            }
        }
      } else {
        Future.successful(NotFound)
      }
  }

  def buscarPorConsecutivoWeb(
      repo_consecutivo: Int,
      empr_id: Long,
      token: String
  ) = Action.async { implicit request: Request[AnyContent] =>
    println("por consecutivo web")
    val secret = config.get[String]("play.http.secret.key")
    if (secret == token) {
      val reporte =
        reporteService.buscarPorConsecutivoWeb(repo_consecutivo, empr_id)
      reporte match {
        case None => {
          Future.successful(NotFound(Json.toJson("{}")))
        }
        case Some(reporte) => {
          Future.successful(Ok(write(reporte)))
        }
      }
    } else {
      Future.successful(NotFound)
    }
  }

  def actualizarReporte() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      println("json: " + json)
      var reporteRequest =
        net.liftweb.json.parse(json.toString).extract[ReporteRequest]
      val coau_codigo = reporteRequest.coau_codigo match {
        case Some(c) => c
        case None    => ""
      }
      val coau_tipo = reporteRequest.coau_tipo match {
        case Some(c) => c
        case None    => 0
      }
      val reporte = reporteRequest.reporte
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val reportenuevo = new Reporte(
        reporte.repo_id,
        reporte.tireuc_id,
        reporte.reti_id,
        reporte.repo_consecutivo,
        reporte.repo_fecharecepcion,
        reporte.repo_direccion,
        reporte.repo_nombre,
        reporte.repo_telefono,
        reporte.repo_fechasolucion,
        reporte.repo_horainicio,
        reporte.repo_horafin,
        reporte.repo_reportetecnico,
        reporte.repo_descripcion,
        reporte.repo_subrepoconsecutivo,
        reporte.rees_id,
        reporte.orig_id,
        reporte.barr_id,
        empr_id,
        reporte.tiba_id,
        usua_id,
        reporte.adicional,
        reporte.meams,
        reporte.eventos,
        reporte.direcciones,
        reporte.novedades
      )
      if (reporteService.actualizar(reportenuevo, coau_tipo, coau_codigo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def actualizarMaterial() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      println("json: " + json)
      var reporteRequest =
        net.liftweb.json.parse(json.toString).extract[ReporteRequest]
      val reporte = reporteRequest.reporte
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val reportenuevo = new Reporte(
        reporte.repo_id,
        reporte.tireuc_id,
        reporte.reti_id,
        reporte.repo_consecutivo,
        reporte.repo_fecharecepcion,
        reporte.repo_direccion,
        reporte.repo_nombre,
        reporte.repo_telefono,
        reporte.repo_fechasolucion,
        reporte.repo_horainicio,
        reporte.repo_horafin,
        reporte.repo_reportetecnico,
        reporte.repo_descripcion,
        reporte.repo_subrepoconsecutivo,
        reporte.rees_id,
        reporte.orig_id,
        reporte.barr_id,
        empr_id,
        reporte.tiba_id,
        usua_id,
        reporte.adicional,
        reporte.meams,
        reporte.eventos,
        reporte.direcciones,
        reporte.novedades
      )
      if (reporteService.actualizarMaterial(reportenuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def actualizarReporteMovil() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      println("Guardar Reporte Movil json: " + json)
      var reporte =
        net.liftweb.json.parse(json.toString).extract[Reporte]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val dateFormatter = new java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
      
      val reportenuevo = new Reporte(
        reporte.repo_id,
        reporte.tireuc_id,
        reporte.reti_id,
        reporte.repo_consecutivo,
        reporte.repo_fecharecepcion,
        reporte.repo_direccion,
        reporte.repo_nombre,
        reporte.repo_telefono,
        reporte.repo_fechasolucion,
        reporte.repo_horainicio,
        reporte.repo_horafin,
        reporte.repo_reportetecnico,
        reporte.repo_descripcion,
        reporte.repo_subrepoconsecutivo,
        reporte.rees_id,
        reporte.orig_id,
        reporte.barr_id,
        empr_id,
        reporte.tiba_id,
        usua_id,
        reporte.adicional,
        reporte.meams,
        reporte.eventos,
        reporte.direcciones,
        reporte.novedades
      )
      println("reporte nuevo: " + reportenuevo)
      if (reporteService.actualizarMovil(reportenuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def actualizarElemento() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      println("json: " + json)
      var reporteRequest =
        net.liftweb.json.parse(json.toString).extract[ReporteRequest]
      val coau_codigo = reporteRequest.coau_codigo match {
        case Some(c) => c
        case None    => ""
      }
      val coau_tipo = reporteRequest.coau_tipo match {
        case Some(c) => c
        case None    => 0
      }
      val reporte = reporteRequest.reporte
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val reportenuevo = new Reporte(
        reporte.repo_id,
        reporte.tireuc_id,
        reporte.reti_id,
        reporte.repo_consecutivo,
        reporte.repo_fecharecepcion,
        reporte.repo_direccion,
        reporte.repo_nombre,
        reporte.repo_telefono,
        reporte.repo_fechasolucion,
        reporte.repo_horainicio,
        reporte.repo_horafin,
        reporte.repo_reportetecnico,
        reporte.repo_descripcion,
        reporte.repo_subrepoconsecutivo,
        reporte.rees_id,
        reporte.orig_id,
        reporte.barr_id,
        empr_id,
        reporte.tiba_id,
        usua_id,
        reporte.adicional,
        reporte.meams,
        reporte.eventos,
        reporte.direcciones,
        reporte.novedades
      )
      if (reporteService
            .actualizarElemento(reportenuevo, coau_tipo, coau_codigo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def actualizarReporteParcial() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var reporte =
        net.liftweb.json.parse((json \ "reporte").toString).extract[Reporte]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val reportenuevo = new Reporte(
        reporte.repo_id,
        reporte.tireuc_id,
        reporte.reti_id,
        reporte.repo_consecutivo,
        reporte.repo_fecharecepcion,
        reporte.repo_direccion,
        reporte.repo_nombre,
        reporte.repo_telefono,
        reporte.repo_fechasolucion,
        reporte.repo_horainicio,
        reporte.repo_horafin,
        reporte.repo_reportetecnico,
        reporte.repo_descripcion,
        reporte.repo_subrepoconsecutivo,
        reporte.rees_id,
        reporte.orig_id,
        reporte.barr_id,
        empr_id,
        reporte.tiba_id,
        usua_id,
        reporte.adicional,
        reporte.meams,
        reporte.eventos,
        reporte.direcciones,
        reporte.novedades
      )
      reporteService.actualizarParcial(reportenuevo).map { result =>
        if (result) {
          Ok(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def borrarReporte(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (reporteService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }

  def convertir(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val _id = reporteService.convertir(id)
      if (_id > 0) {
        Future.successful(Ok(Json.toJson(_id)))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson(0)))
      }
  }

  def actualizarHistoria() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      reporteService.actualizarHistoria(empr_id.get).map { respuesta =>
        if (respuesta) {
          Ok(Json.toJson("true"))
        } else {
          ServiceUnavailable(Json.toJson("false"))
        }
      }
  }

  def validarCodigo(elem_id: scala.Long, codigo: String) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val resultado = reporteService.validarCodigo(elem_id, codigo, empr_id.get)
      Future.successful(Ok(Json.toJson(resultado)))
    }

  def siap_reporte_vencido(): Action[AnyContent] =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      reporteService.siap_reporte_vencido(empr_id.get).map { reportes =>
        Ok(write(reportes))
      }
    }

  def imprimirReporte(id: Long, empr_id: Long) = Action {
    val os = reporteService.imprimir(id, empr_id)
    Ok(os).as("application/pdf")
  }

  def imprimirRelacion(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: Long,
      usua_id: Long,
      formato: String
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val os = reporteService.imprimirRelacion(
      fecha_inicial,
      fecha_final,
      empr_id,
      usua_id,
      formato
    )
    val fi = new DateTime(fecha_inicial)
    val ff = new DateTime(fecha_final)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "OyM_Pendientes_Entre" + fmt.print(fi) + "_y_" + fmt
      .print(ff) + (formato match {
      case "pdf" => ".pdf"
      case "xls" => ".xlsx"
    })
    val attach = "attachment; filename=" + filename
    Future.successful(formato match {
      case "pdf" => Ok(os).as("application/pdf").withHeaders("Content-Disposition" -> attach)
      case "xls" =>
        Ok(os)
          .as(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          )
          .withHeaders("Content-Disposition" -> attach)
    })
  }

  def imprimirRelacionFiltrado(
      cuad_id: Long,
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: Long,
      usua_id: Long,
      formato: String
  ) = authenticatedUserAction.async {
    val os = reporteService.imprimirRelacionFiltrado(
      cuad_id,
      fecha_inicial,
      fecha_final,
      empr_id,
      usua_id,
      formato
    )
    val fi = new DateTime(fecha_inicial)
    val ff = new DateTime(fecha_final)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "OyM_Pendientes_Entre" + fmt.print(fi) + "_y_" + fmt
      .print(ff) + (formato match {
      case "pdf" => ".pdf"
      case "xls" => ".xlsx"
    })
    val attach = "attachment; filename=" + filename
    Future.successful(formato match {
      case "pdf" => Ok(os).as("application/pdf").withHeaders("Content-Disposition" -> attach)
      case "xls" =>
        Ok(os)
          .as(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          )
          .withHeaders("Content-Disposition" -> attach)
    })
  }

  def imprimirEjecutado(
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: Long,
      usua_id: Long,
      formato: String
  ) = authenticatedUserAction.async {
    val os = reporteService.imprimirEjecutado(
      fecha_inicial,
      fecha_final,
      empr_id,
      usua_id,
      formato
    )
    val fi = new DateTime(fecha_inicial)
    val ff = new DateTime(fecha_final)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "OyM_Ejecutadas_Entre" + fmt.print(fi) + "_y_" + fmt
      .print(ff) + (formato match {
      case "pdf" => ".pdf"
      case "xls" => ".xlsx"
    })
    val attach = "attachment; filename=" + filename
    Future.successful(formato match {
      case "pdf" => Ok(os).as("application/pdf").withHeaders("Content-Disposition" -> attach)
      case "xls" =>
        Ok(os)
          .as(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          )
          .withHeaders("Content-Disposition" -> attach)
    })
  }

  def imprimirEjecutadoFiltrado(
      cuad_id: Long,
      fecha_inicial: Long,
      fecha_final: Long,
      empr_id: Long,
      usua_id: Long,
      formato: String
  ) = Action {
    val os = reporteService.imprimirEjecutadoFiltrado(
      cuad_id,
      fecha_inicial,
      fecha_final,
      empr_id,
      usua_id,
      formato
    )
    val fi = new DateTime(fecha_inicial)
    val ff = new DateTime(fecha_final)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val filename = "OyM_Ejecutadas_Entre" + fmt.print(fi) + "_y_" + fmt
      .print(ff) + (formato match {
      case "pdf" => ".pdf"
      case "xls" => ".xlsx"
    })
    val attach = "attachment; filename=" + filename
    formato match {
      case "pdf" => Ok(os).as("application/pdf")
      case "xls" =>
        Ok(os)
          .as(
            "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
          )
          .withHeaders("Content-Disposition" -> attach)
    }

  }  

  def imprimirFormato(reti_id: Long, empr_id: Long) = Action {
    val os = reporteService.formato(reti_id, empr_id)
    Ok(os).as("application/pdf")
  }

  def ActaDesmonteXls(fecha_corte: Long, tireuc_id: Int) =
    authenticatedUserAction.async { implicit request =>
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val response = reporteService.ActaDesmonteXls(
        fecha_corte,
        tireuc_id,
        empr_id.get,
        usua_id.get
      )
      if (response._1 > 0) {
        val filename = "Acta_de_Desmonte_No." + response._1 + ".xlsx"
        val attach = "attachment; filename=" + filename
        Future.successful(
          Ok(response._2)
            .as(
              "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            )
            .withHeaders("Content-Disposition" -> attach)
        )
      } else {
        Future.successful(NotFound(Json.toJson("false")))
      }
    }

  def validarReporteDiligenciado(reti_id: scala.Long, repo_consecutivo: Int) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val valido = reporteService.validarReporteDiligenciado(
        reti_id,
        repo_consecutivo,
        empr_id.get
      )
      Future.successful(Ok(Json.toJson(valido)))
    }

  def reporteSinOrdenTrabajo(fecha_inicial: Long, fecha_final: Long) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      reporteService
        .reporteSinOrdenTrabajo(fecha_inicial, fecha_final, empr_id.get)
        .map { datos =>
          Ok(write(datos))
        }
    }

  def getFotoData(fileName: String) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      var filePath = "/opt/siap/fotos/" + fileName
      var pathToImage = Paths.get(filePath);
      var bytes = Files.readAllBytes(pathToImage);
      var encondedString = Base64.getEncoder().encodeToString(bytes);
      Future.successful(Ok(encondedString))
  }

  def getReportesParaCierreDirecto(fecha_inicial: Long, fecha_final: Long) = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    reporteService.getReportesParaCierreDirecto(fecha_inicial, fecha_final, empr_id.get).map { datos =>
      println("Datos: " + datos)
      Ok(write(datos))
    }
  }

  def cerrarReporte(tireuc_id: Long, repo_id: Long) = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)
    val result = reporteService.cerrarReporte(tireuc_id, repo_id, empr_id.get, usua_id.get)
    Future.successful(Ok(write(result)))
  }

  def cerrarReportes = authenticatedUserAction.async {
    implicit request =>
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val _lista = request.body.asJson.get.as[List[(Long, Long)]]
      for (item <- _lista){ 
        reporteService.cerrarReporte(item._1, item._2, empr_id.get, usua_id.get)
      }
      Future.successful(Ok(write("true")))
  }
}
