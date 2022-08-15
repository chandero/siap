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
class TipoBarrioController @Inject()(
    tibaService: TipoBarrioRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = tibaService.cuenta()
      tibaService.todos(page_size, current_page).map { tiposbarrio =>
        Ok(Json.obj("tiposbarrio" -> tiposbarrio, "total" -> total))
      }
    }

  def tiposbarrio(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    tibaService.tiposbarrio().map { tiposbarrio =>
      Ok(Json.toJson(tiposbarrio))
    }
  } 

  def buscarPorId(tiba_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val tipobarrio = tibaService.buscarPorId(tiba_id)
      tipobarrio match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarTipoBarrio() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tipobarrio = json.as[TipoBarrio]
      val usua_id = Utility.extraerUsuario(request)
      val tipobarrionuevo = new TipoBarrio(Some(0),
                                   tipobarrio.tiba_descripcion,
                                   tipobarrio.tiba_estado,
                                   usua_id.get)
      tibaService.crear(tipobarrionuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarTipoBarrio() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tipobarrio = json.as[TipoBarrio]
      val usua_id = Utility.extraerUsuario(request)
      val tipobarrionuevo = new TipoBarrio(
        tipobarrio.tiba_id,
        tipobarrio.tiba_descripcion,
        tipobarrio.tiba_estado,
        usua_id.get
      )
      if (tibaService.actualizar(tipobarrionuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarTipoBarrio(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (tibaService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
