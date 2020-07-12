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
class TipoRetiroController @Inject()(tireService: TipoRetiroRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def buscarPorId(tire_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val tire = tireService.buscarPorId(tire_id)
      tire match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(tire) => {
          Future.successful(Ok(Json.toJson(tire)))
        }
      }             
    }

    def todos(p:Long, c:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        tireService.todos(p, c).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def tiposretiro() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        tireService.tiposretiro().map { result =>
           Ok(Json.toJson(result))
        }
    }

    def guardar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tire = json.as[TipoRetiro]
      val usua_id = Utility.extraerUsuario(request)
      val tirenuevo = new TipoRetiro(Some(0),tire.tire_descripcion,tire.tire_estado,usua_id.get)
      tireService.crear(tirenuevo).map { result =>
        if (result > 0){
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
    }

    def actualizar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tire = json.as[TipoRetiro]
      val usua_id = Utility.extraerUsuario(request)
      val tirenuevo = new TipoRetiro(tire.tire_id,tire.tire_descripcion,tire.tire_estado,usua_id.get)
      if (tireService.actualizar(tirenuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }    

    def borrar(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        val usua_id = Utility.extraerUsuario(request)
        if (tireService.borrar(id, usua_id.get)) {
            Future.successful(Ok(Json.toJson("true")))
        } else {
            Future.successful(ServiceUnavailable(Json.toJson("false")))
        }
    }
}