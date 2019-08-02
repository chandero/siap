package controllers

import javax.inject.Inject

import models._

import play.api.mvc._
import play.api.libs.json._

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

import pdi.jwt.JwtSession

import utilities._

@Singleton
class ActividadController @Inject()(actividadService: ActividadRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def buscarPorId(acti_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val actividad = actividadService.buscarPorId(acti_id)
      actividad match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(actividad) => {
          Future.successful(Ok(Json.toJson(actividad)))
        }
      }             
    }

    def todos(p:Long, c:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        actividadService.todos(p, c).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def actividades() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        actividadService.actividades().map { result =>
           Ok(Json.toJson(result))
        }
    }

    def guardarActividad() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var actividad = json.as[Actividad]
      val usua_id = Utility.extraerUsuario(request)
      val actividadnuevo = new Actividad(Some(0),actividad.acti_descripcion,actividad.acti_estado,usua_id.get)
      actividadService.crear(actividadnuevo).map { result =>
        if (result > 0){
          Created(Json.toJson(result))
        } else {
          NotAcceptable(Json.toJson(0))
        }
      }
    }

    def actualizarActividad() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var actividad = json.as[Actividad]
      val usua_id = Utility.extraerUsuario(request)
      val actividadnuevo = new Actividad(actividad.acti_id,actividad.acti_descripcion,actividad.acti_estado,usua_id.get)
      if (actividadService.actualizar(actividadnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }    

    def borrarActividad(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        val usua_id = Utility.extraerUsuario(request)
        if (actividadService.borrar(id, usua_id.get)) {
            Future.successful(Ok(Json.toJson("true")))
        } else {
            Future.successful(ServiceUnavailable(Json.toJson("false")))
        }
    }
}