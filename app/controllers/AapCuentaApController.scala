package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import pdi.jwt.JwtSession

import utilities._

import dto.ResultDto

@Singleton
class AapCuentaApController @Inject()(
    aacuService: AapCuentaApRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = aacuService.cuenta()
      aacuService.todos(page_size, current_page).map { aapcuentasap =>
        Ok(Json.obj("aapcuentasap" -> aapcuentasap, "total" -> total))
      }
    }

  def aapcuentasap(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    aacuService.aapcuentasap(empr_id.get).map { aapcuentasap =>
      Ok(Json.toJson(aapcuentasap))
    }
  } 

  def buscarPorId(aacu_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val aapcuentaap = aacuService.buscarPorId(aacu_id)
      aapcuentaap match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarAapCuentaAp() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapcuentaap = json.as[AapCuentaAp]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val aapcuentaapnuevo = new AapCuentaAp(Some(0),
                                   aapcuentaap.aacu_descripcion,
                                   aapcuentaap.aacu_estado,
                                   empr_id.get,
                                   usua_id.get)
      aacuService.crear(aapcuentaapnuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAapCuentaAp() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapcuentaap = json.as[AapCuentaAp]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val aapcuentaapnuevo = new AapCuentaAp(
        aapcuentaap.aacu_id,
        aapcuentaap.aacu_descripcion,
        aapcuentaap.aacu_estado,
        empr_id.get,
        usua_id.get
      )
      if (aacuService.actualizar(aapcuentaapnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAapCuentaAp(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (aacuService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}