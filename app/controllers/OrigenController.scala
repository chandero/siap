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
class OrigenController @Inject()(origenService: OrigenRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def buscarPorId(orig_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val origen = origenService.buscarPorId(orig_id)
      origen match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(origen) => {
          Future.successful(Ok(Json.toJson(origen)))
        }
      }             
    }

    def todos(p:Long, c:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        origenService.todos(p, c).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def origenes() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        origenService.origenes().map { result =>
           Ok(Json.toJson(result))
        }
    }

    def guardarOrigen() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var origen = json.as[Origen]
      val usua_id = Utility.extraerUsuario(request)
      val origennuevo = new Origen(Some(0),origen.orig_descripcion,origen.orig_estado,usua_id.get)
      origenService.crear(origennuevo).map { result =>
        if (result > 0){
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
    }

    def actualizarOrigen() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var origen = json.as[Origen]
      val usua_id = Utility.extraerUsuario(request)
      val origennuevo = new Origen(origen.orig_id,origen.orig_descripcion,origen.orig_estado,usua_id.get)
      if (origenService.actualizar(origennuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }    

    def borrarOrigen(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        val usua_id = Utility.extraerUsuario(request)
        if (origenService.borrar(id, usua_id.get)) {
            Future.successful(Ok(Json.toJson("true")))
        } else {
            Future.successful(ServiceUnavailable(Json.toJson("false")))
        }
    }
}