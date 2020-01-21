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
class TipoActivoController @Inject()(tiacService: TipoActivoRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def buscarPorId(tiac_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val tiac = tiacService.buscarPorId(tiac_id)
      tiac match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(tiac) => {
          Future.successful(Ok(Json.toJson(tiac)))
        }
      }             
    }

    def todos(p:Long, c:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        tiacService.todos(p, c).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def tiposretiro() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        tiacService.tiposretiro().map { result =>
           Ok(Json.toJson(result))
        }
    }

    def guardar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tiac = json.as[TipoActivo]
      val usua_id = Utility.extraerUsuario(request)
      val tiacnuevo = new TipoActivo(Some(0),tiac.tiac_descripcion,tiac.tiac_estado,usua_id.get)
      tiacService.crear(tiacnuevo).map { result =>
        if (result > 0){
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
    }

    def actualizar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tiac = json.as[TipoActivo]
      val usua_id = Utility.extraerUsuario(request)
      val tiacnuevo = new TipoActivo(tiac.tiac_id,tiac.tiac_descripcion,tiac.tiac_estado,usua_id.get)
      if (tiacService.actualizar(tiacnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }    

    def borrar(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        val usua_id = Utility.extraerUsuario(request)
        if (tiacService.borrar(id, usua_id.get)) {
            Future.successful(Ok(Json.toJson("true")))
        } else {
            Future.successful(ServiceUnavailable(Json.toJson("false")))
        }
    }
}