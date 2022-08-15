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
class AapMedidorMarcaController @Inject()(
    amemService: AapMedidorMarcaRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = amemService.cuenta()
      amemService.todos(page_size, current_page).map { aapmedidormarcas =>
        Ok(Json.obj("aapmedidormarcas" -> aapmedidormarcas, "total" -> total))
      }
    }

  def aapmedidormarcas(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    amemService.aapmedidormarcas().map { aapmedidormarcas =>
      Ok(Json.toJson(aapmedidormarcas))
    }
  } 

  def buscarPorId(amem_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val aapmedidormarca = amemService.buscarPorId(amem_id)
      aapmedidormarca match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarAapMedidorMarca() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmedidormarca = json.as[AapMedidorMarca]
      val usua_id = Utility.extraerUsuario(request)
      val aapmedidormarcanuevo = new AapMedidorMarca(Some(0),
                                   aapmedidormarca.amem_descripcion,
                                   aapmedidormarca.amem_estado,
                                   usua_id.get)
      amemService.crear(aapmedidormarcanuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAapMedidorMarca() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmedidormarca = json.as[AapMedidorMarca]
      val usua_id = Utility.extraerUsuario(request)
      val aapmedidormarcanuevo = new AapMedidorMarca(
        aapmedidormarca.amem_id,
        aapmedidormarca.amem_descripcion,
        aapmedidormarca.amem_estado,
        usua_id.get
      )
      if (amemService.actualizar(aapmedidormarcanuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAapMedidorMarca(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (amemService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}