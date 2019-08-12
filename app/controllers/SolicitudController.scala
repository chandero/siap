package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import play.api.Configuration

import pdi.jwt.JwtSession

import utilities._

import dto.ResultDto

@Singleton
class SolicitudController @Inject()(
    soliService: SolicitudRepository,
    cc: ControllerComponents,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = soliService.cuenta()
      soliService.todos(page_size, current_page).map { solicitudes =>
        Ok(Json.obj("solicitudes" -> solicitudes, "total" -> total))
      }
    }

  def solis(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    soliService.solis().map { solicitudes =>
      Ok(Json.toJson(solicitudes))
    }
  } 

  def buscarPorId(soli_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val soli = soliService.buscarPorId(soli_id)
      soli match {
        case None => {
          Future.successful(BadRequest(Json.toJson("false")))
        }
        case Some(soli) => {
          soli.a.soli_id match {
            case Some(soli_id) => Future.successful(Ok(Json.toJson(soli)))
            case None => Future.successful(BadRequest(Json.toJson("false")))
          }
        }
      }
  }

  def buscarPorRadicado(soli_radicado: String) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      var empr_id = Utility.extraerEmpresa(request)
      val soli = soliService.buscarPorRadicado(soli_radicado, empr_id.get)
      soli match {
        case None => {
          Future.successful(BadRequest(Json.toJson("false")))
        }
        case Some(soli) => {
          soli.a.soli_id match {
            case Some(soli_id) => Future.successful(Ok(Json.toJson(soli)))
            case None => Future.successful(BadRequest(Json.toJson("false")))
          }
        }
      }
  }

  def buscarPorRango(anho: Int, mes: Int) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
    var empr_id = Utility.extraerEmpresa(request)
    soliService.buscarPorRango(anho, mes, empr_id.get).map { solicitudes =>
      Ok(Json.toJson(solicitudes))
    }
  }

  def buscarPorVencer() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
    var empr_id = Utility.extraerEmpresa(request)
    soliService.buscarPorVencer(empr_id.get).map { solicitudes =>
      Ok(Json.toJson(solicitudes))
    }
  }
  
  def guardar() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var soli = json.as[Solicitud]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val a = new SolicitudA(Some(0),                                    
                                 soli.a.soti_id,
                                 soli.a.soli_fecha,
                                 soli.a.soli_nombre,
                                 soli.a.soli_radicado,
                                 soli.a.soli_direccion,
                                 soli.a.barr_id,
                                 soli.a.soli_telefono,
                                 soli.a.soli_email,
                                 soli.a.soli_solicitud,
                                 soli.a.soli_respuesta,
                                 soli.a.soli_informe,
                                 soli.a.soli_consecutivo)
      val b = new SolicitudB(soli.b.soli_fecharespuesta,
                             soli.b.soli_fechadigitado,
                             soli.b.soli_fechalimite,
                             soli.b.soli_fechasupervisor,
                             soli.b.soli_fechainforme,
                             soli.b.soli_fechavisita,
                             soli.b.soli_fecharte,
                             soli.b.soli_fechaalmacen,
                             soli.b.soli_numerorte,
                             soli.b.soli_puntos,
                             soli.b.soli_tipoexpansion,
                             soli.b.soli_aprobada,
                             soli.b.soli_codigorespuesta,
                             soli.b.soli_luminarias,                       
                             soli.b.soli_estado,
                             empr_id,
                             usua_id)
      val solinuevo = new Solicitud(a,b)
      soliService.crear(solinuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson(result))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizar() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var soli = json.as[Solicitud]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val a = new SolicitudA(soli.a.soli_id,
                              soli.a.soti_id,
                                 soli.a.soli_fecha,
                                 soli.a.soli_nombre,
                                 soli.a.soli_radicado,
                                 soli.a.soli_direccion,
                                 soli.a.barr_id,
                                 soli.a.soli_telefono,
                                 soli.a.soli_email,
                                 soli.a.soli_solicitud,
                                 soli.a.soli_respuesta,
                                 soli.a.soli_informe,
                                 soli.a.soli_consecutivo)
      val b = new SolicitudB(soli.b.soli_fecharespuesta,
                             soli.b.soli_fechadigitado,
                             soli.b.soli_fechalimite,
                             soli.b.soli_fechasupervisor,
                             soli.b.soli_fechainforme,
                             soli.b.soli_fechavisita,
                             soli.b.soli_fecharte,
                             soli.b.soli_fechaalmacen,
                             soli.b.soli_numerorte,
                             soli.b.soli_puntos,
                             soli.b.soli_tipoexpansion,
                             soli.b.soli_aprobada,      
                             soli.b.soli_codigorespuesta,
                             soli.b.soli_luminarias,                       
                             soli.b.soli_estado,
                             empr_id,
                             usua_id)
      val solinuevo = new Solicitud(a,b)
      if (soliService.actualizar(solinuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrar(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      if (soliService.borrar(id, empr_id.get, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }

  def entregarSupervisor(soli_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] => 
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      if (soliService.entregarSupervisor(soli_id, empr_id.get, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }    
  }

  def entregarFormatoRTE(soli_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] => 
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      if (soliService.entregarFormatoRTE(soli_id, empr_id.get, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }    
  }

  def asignarRte(soli_id: Long, soli_fechaalmacen: Long, soli_numerorte: Int) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] => 
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      if (soliService.asignarRte(soli_id, soli_fechaalmacen, soli_numerorte, empr_id.get, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }    
  }


  def ingresarInforme() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] => 
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val json = request.body.asJson.get
      var soli_id = (json \ "soli_id").as[Long]
      var info = (json \ "info").as[String]
      if (soliService.ingresarInforme(soli_id, info, empr_id.get, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }     
  }

  def ingresarRespuesta() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] => 
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val json = request.body.asJson.get
      var soli_id = (json \ "soli_id").as[Long]
      var info = (json \ "info").as[String]
      if (soliService.ingresarRespuesta(soli_id, info, empr_id.get, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }

  def formatoRTE(soli_id: Long, empr_id: Long, token: String) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
       val os = soliService.formatoRTE(soli_id, empr_id)
       Ok(os).as("application/pdf")
    } else {
       Forbidden("Dude, you’re not logged in.")
    }
  }

  def imprimirRespuesta(soli_id: Long, empr_id: Long, firma: Int, token: String) = Action {
    if (config.get[String]("play.http.secret.key") == token) {
       val os = soliService.imprimirRespuesta(soli_id, empr_id, firma)
       Ok(os).as("application/pdf")
    } else {
       Forbidden("Dude, you’re not logged in.")
    }
  }

}
