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
class TipoMedidorController @Inject()(
    timeService: TipoMedidorRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = timeService.cuenta()
      timeService.todos(page_size, current_page).map { tiposmedidor =>
        Ok(Json.obj("tiposmedidor" -> tiposmedidor, "total" -> total))
      }
    }

  def tiposmedidor(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    timeService.tiposmedidor().map { tiposmedidor =>
      Ok(Json.toJson(tiposmedidor))
    }
  } 

  def buscarPorId(amet_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val tipomedidor = timeService.buscarPorId(amet_id)
      tipomedidor match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarTipoMedidor() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tipomedidor = json.as[TipoMedidor]
      val usua_id = Utility.extraerUsuario(request)
      val tipomedidornuevo = new TipoMedidor(Some(0),
                                   tipomedidor.amet_descripcion,
                                   tipomedidor.amet_estado,
                                   usua_id.get)
      timeService.crear(tipomedidornuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarTipoMedidor() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tipomedidor = json.as[TipoMedidor]
      val usua_id = Utility.extraerUsuario(request)
      val tipomedidornuevo = new TipoMedidor(
        tipomedidor.amet_id,
        tipomedidor.amet_descripcion,
        tipomedidor.amet_estado,
        usua_id.get
      )
      if (timeService.actualizar(tipomedidornuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarTipoMedidor(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (timeService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
