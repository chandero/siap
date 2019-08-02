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
class AapUsoController @Inject()(
    aausService: AapUsoRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = aausService.cuenta()
      aausService.todos(page_size, current_page).map { aapusos =>
        Ok(Json.obj("aapusos" -> aapusos, "total" -> total))
      }
    }

  def aapusos(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    aausService.aapusos().map { aapusos =>
      Ok(Json.toJson(aapusos))
    }
  } 

  def buscarPorId(aaus_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val aapuso = aausService.buscarPorId(aaus_id)
      aapuso match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarAapUso() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapuso = json.as[AapUso]
      val usua_id = Utility.extraerUsuario(request)
      val aapusonuevo = new AapUso(Some(0),
                                   aapuso.aaus_descripcion,
                                   aapuso.aaus_estado,
                                   usua_id.get)
      aausService.crear(aapusonuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAapUso() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapuso = json.as[AapUso]
      val usua_id = Utility.extraerUsuario(request)
      val aapusonuevo = new AapUso(
        aapuso.aaus_id,
        aapuso.aaus_descripcion,
        aapuso.aaus_estado,
        usua_id.get
      )
      if (aausService.actualizar(aapusonuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAapUso(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (aausService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}