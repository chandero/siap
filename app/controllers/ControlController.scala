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
class ControlController @Inject()(mService: ControlRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def buscarPorId(aap_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val m = mService.buscarPorId(aap_id, empr_id.get)
      m match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(m) => {
          Future.successful(Ok(Json.toJson(m)))
        }
      }             
    }

    def todos(p:Long, c:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        mService.todos(p, c, empr_id.get).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def controles() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        mService.controles(empr_id.get).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def guardar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var m = json.as[Control]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val mnuevo = new Control(Some(0),empr_id, usua_id, m.aap_direccion, m.barr_id, m.esta_id)
      mService.crear(mnuevo).map { result =>
        if (result > 0){
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
    }

    def actualizar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var m = json.as[Control]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)      
      val mnuevo = new Control(m.aap_id, empr_id, usua_id, m.aap_direccion, m.barr_id, m.esta_id)
      if (mService.actualizar(mnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }    

    def borrar(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        val usua_id = Utility.extraerUsuario(request)
        val empr_id = Utility.extraerEmpresa(request)
        if (mService.borrar(id, empr_id.get, usua_id.get)) {
            Future.successful(Ok(Json.toJson("true")))
        } else {
            Future.successful(ServiceUnavailable(Json.toJson("false")))
        }
    }

  def buscarParaVerificar(aap_id:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    val result = mService.buscarParaVerificar(aap_id, empr_id.get)
    Future.successful(Ok(Json.toJson(result)))
  }
  
  def buscarParaEditar(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    val activo = mService.buscarParaEditar(id, empr_id.get)
    activo match {
      case None => {
        Future.successful(NotFound(Json.toJson("false")))
      }
      case Some(activo) => {
        Future.successful(Ok(Json.toJson(activo)))
      }
    }
  }  

}