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
class AapMedidorTipoController @Inject()(
    ametService: AapMedidorTipoRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = ametService.cuenta()
      ametService.todos(page_size, current_page).map { aapmedidortipos =>
        Ok(Json.obj("aapmedidortipos" -> aapmedidortipos, "total" -> total))
      }
    }

  def aapmedidortipos(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    ametService.aapmedidortipos().map { aapmedidortipos =>
      Ok(Json.toJson(aapmedidortipos))
    }
  } 

  def buscarPorId(amet_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val aapmedidormarca = ametService.buscarPorId(amet_id)
      aapmedidormarca match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarAapMedidorTipo() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmedidormarca = json.as[AapMedidorTipo]
      val usua_id = Utility.extraerUsuario(request)
      val aapmedidormarcanuevo = new AapMedidorTipo(Some(0),
                                   aapmedidormarca.amet_descripcion,
                                   aapmedidormarca.amet_estado,
                                   usua_id.get)
      ametService.crear(aapmedidormarcanuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAapMedidorTipo() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmedidormarca = json.as[AapMedidorTipo]
      val usua_id = Utility.extraerUsuario(request)
      val aapmedidormarcanuevo = new AapMedidorTipo(
        aapmedidormarca.amet_id,
        aapmedidormarca.amet_descripcion,
        aapmedidormarca.amet_estado,
        usua_id.get
      )
      if (ametService.actualizar(aapmedidormarcanuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAapMedidorTipo(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (ametService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}