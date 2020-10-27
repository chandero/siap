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
class NovedadController @Inject()(service: NovedadRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def buscarPorId(tire_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val novedad = service.buscarPorId(tire_id)
      novedad match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(novedad) => {
          Future.successful(Ok(Json.toJson(novedad)))
        }
      }             
    }

    def todos(p:Long, c:Long, t:Int) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        service.todos(p, c, t).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def novedades(t: Int) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        service.novedades(t).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def guardar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var novedad = json.as[Novedad]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val nnuevo = new Novedad(Some(0),novedad.nove_descripcion,novedad.nove_estado, novedad.nove_tipo, empr_id, usua_id)
      service.crear(nnuevo).map { result =>
        if (result > 0){
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
    }

    def actualizar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var novedad = json.as[Novedad]
      val usua_id = Utility.extraerUsuario(request)
      var empr_id = Utility.extraerEmpresa(request)
      val nnuevo = new Novedad(novedad.nove_id,novedad.nove_descripcion, novedad.nove_estado, novedad.nove_tipo, empr_id, usua_id)
      if (service.actualizar(nnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }    

    def borrar(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        val usua_id = Utility.extraerUsuario(request)
        if (service.borrar(id, usua_id.get)) {
            Future.successful(Ok(Json.toJson("true")))
        } else {
            Future.successful(ServiceUnavailable(Json.toJson("false")))
        }
    }
}