package controllers

import javax.inject.Inject
import java.io.{OutputStream, ByteArrayOutputStream}

import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import org.joda.time.LocalDate
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import pdi.jwt.JwtSession

import utilities._

import dto._

@Singleton
class ControlReporteController @Inject()(
    reporteService: ControlReporteRepository,
    cc: ControllerComponents,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(DateTimeSerializer)           
  def todos(): Action[AnyContent] =
    authenticatedUserAction.async { implicit request: Request[AnyContent] => 
    val json = request.body.asJson.get
    val page_size = ( json \ "page_size").as[Long]
    val current_page = ( json \ "current_page").as[Long]
    val orderby = ( json \ "orderby").as[String]
    val filter = ( json \ "filter").as[QueryDto]
    var filtro = Utility.procesarFiltrado(filter)
    if (filtro == "()") {
      filtro = ""
    }
    val empr_id = Utility.extraerEmpresa(request)
    val total = reporteService.cuenta(empr_id.get)
    reporteService.todos(page_size, current_page, empr_id.get, orderby, filtro).map { reportes =>
        Ok(write( new ReporteResult(reportes, total)))
      }
    }

  def reportes(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    reporteService.reportes(empr_id.get).map { reportes =>
      Ok(write(reportes))
    }
  }

  def estados(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    reporteService.estados().map { estados =>
      Ok(Json.toJson(estados))
    }
  }

  def tipos(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
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
          println("Reporte: " + reporte)
          Future.successful(Ok(write(reporte)))
        }
      }
  }

  def buscarPorConsecutivo(reti_id: Long, repo_consecutivo: Int) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val reporte = reporteService.buscarPorConsecutivoConsulta(reti_id, repo_consecutivo, empr_id.get)
      reporte match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(reporte) => {
          Future.successful(Ok(write(reporte)))
        }
      }
  }

  def buscarPorRango(anho: Int, mes: Int, tireuc_id: Int) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      reporteService.buscarPorRango(anho, mes, tireuc_id, empr_id.get).map { reportes =>
        Ok(write(reportes))
      }
  }


  def guardarReporte() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var reporte = net.liftweb.json.parse(json.toString).extract[Reporte]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val reportenuevo = new Reporte(null,
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
      reporteService.crear(reportenuevo).map { case (id, consec) =>
        if (id > 0) {
          Created(Json.obj("id" ->id, "consec" -> consec))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def guardarReporteWeb() = Action.async {
    implicit request: Request[AnyContent] =>
      implicit val dateTimeJsReader = JodaReads.jodaDateReads("yyyy-MM-dd HH:mm:ss")
      val json = request.body.asJson.get
      println("Json: " + json)
      val secret = config.get[String]("play.http.secret.key")
      val token = (json \ "token").as[String]
      if (secret == token) {
        val repo_direccion = (json \ "repo_direccion").as[String]
        val barr_id = (json \ "barr_id").as[Long]
        val repo_nombre = (json \ "repo_nombre").as[String]
        val repo_telefono = (json \ "repo_telefono").as[String]
        val repo_email = (json \ "repo_email").as[String]
        val repo_descripcion = (json \ "repo_descripcion").as[String]
        val repo_fecharecepcion = (json \ "repo_fecharecepcion").as[DateTime]
        var repo_fechadigitacion = new DateTime()
        val acti_id = (json \ "acti_id").as[Long]
        val empr_id = (json \ "empr_id").as[Long]
        var ra = new ReporteAdicional(None,
                                      None,
                                      None,                             
                                      None,
                                      None,
                                      None,
                                      None,
                                      None,
                                      None,
                                      Some(repo_email),
                                      Some(acti_id),
                                      None,
                                      None,
                                      None,
                                      None,
                                      None,
                                      None,
                                      None,
                                      None,
                                      None,
                                      None)
        val usua_id = 1
        val reportenuevo = new Reporte(null,
                              Some(2),
                              Some(1),
                              None,
                              Some(repo_fecharecepcion),
                              Some(repo_direccion),
                              Some(repo_nombre),
                              Some(repo_telefono),
                              None,
                              None,
                              None,
                              None,
                              Some(repo_descripcion),
                              None,
                              Some(1),
                              Some(9),
                              Some(barr_id),
                              Some(empr_id),
                              None,
                              Some(usua_id),
                              Some(ra),
                              None,
                              None,
                              None,
                              None
                  )
        reporteService.crear(reportenuevo).map { case (id, consec) =>
          if (id > 0) {
            Created(Json.obj("id" ->id, "consec" -> consec))
          } else {
            NotAcceptable(Json.toJson("error"))
          }
        }
      } else {
        Future.successful(NotFound)
      }
  }

  def buscarPorConsecutivoWeb(repo_consecutivo: Int, empr_id: Long, token: String) = Action.async {
    implicit request: Request[AnyContent] =>
     val secret = config.get[String]("play.http.secret.key")
     if (secret == token) {
      val reporte = reporteService.buscarPorConsecutivoWeb(repo_consecutivo, empr_id)
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
      var reporteRequest = net.liftweb.json.parse(json.toString).extract[ReporteRequest]
      val coau_codigo = reporteRequest.coau_codigo match {
        case Some(c) => c
        case None => ""
      }
      val coau_tipo = reporteRequest.coau_tipo match {
        case Some(c) => c
        case None => 0
      }
      val reporte = reporteRequest.reporte
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)      
      val reportenuevo = new Reporte(reporte.repo_id,
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
                            reporte.novedades)
      if (reporteService.actualizar(reportenuevo, coau_tipo, coau_codigo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
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
      val _id =reporteService.convertir(id)
      if (_id > 0) {
        Future.successful(Ok(Json.toJson(_id)))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson(0)))
      }
  }

  def actualizarHistoria() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    if (reporteService.actualizarHistoria(empr_id.get)) {
      Future.successful(Ok(Json.toJson("true"))) 
    } else {
      Future.successful(ServiceUnavailable(Json.toJson("false")))
    }
  }

  def validarCodigo(elem_id: scala.Long, codigo: String) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    val resultado = reporteService.validarCodigo(elem_id, codigo, empr_id.get)
    Future.successful(Ok(Json.toJson(resultado)))
  }

  
  def siap_reporte_vencido(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    reporteService.siap_reporte_vencido(empr_id.get).map { reportes =>
      Ok(write(reportes))
    }
  }

  def imprimirReporte(id: Long, empr_id: Long) = Action {
     val os = reporteService.imprimir(id, empr_id)
     Ok(os).as("application/pdf")
  }
   
  def imprimirRelacion(fecha_inicial: Long, fecha_final: Long, empr_id: Long, usua_id: Long, tipo: String) = Action {
     val os = reporteService.imprimirRelacion(fecha_inicial, fecha_final, empr_id, usua_id, tipo)
     val fi = new DateTime(fecha_inicial)
     val ff = new DateTime(fecha_final)
     val fmt = DateTimeFormat.forPattern("yyyyMMdd")
     val filename = "OyMPendientes_Entre" + fmt.print(fi) + "_y_" + fmt.print(ff) + ".xlsx"
     val attach = "attachment; filename=" + filename
     tipo match {
       case "pdf" => Ok(os).as("application/pdf")
       case "xls" => Ok(os).as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet").withHeaders("Content-Disposition" -> attach )
     }
     
  }

  def imprimirFormato(reti_id: Long, empr_id: Long) = Action {
     val os = reporteService.formato(reti_id, empr_id)
     Ok(os).as("application/pdf")
  }  

  def validarReporteDiligenciado(reti_id: scala.Long, repo_consecutivo: Int) = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    val valido = reporteService.validarReporteDiligenciado(reti_id, repo_consecutivo, empr_id.get)
    Future.successful(Ok(Json.toJson(valido)))
  }
}
