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
class AapModeloController @Inject()(
    aamoService: AapModeloRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = aamoService.cuenta()
      aamoService.todos(page_size, current_page).map { aapmodelos =>
        Ok(Json.obj("aapmodelos" -> aapmodelos, "total" -> total))
      }
    }

  def aapmodelos(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    aamoService.aapmodelos().map { aapmodelos =>
      Ok(Json.toJson(aapmodelos))
    }
  } 

  def buscarPorId(aamo_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val aapmedidormarca = aamoService.buscarPorId(aamo_id)
      aapmedidormarca match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarAapModelo() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmedidormarca = json.as[AapModelo]
      val usua_id = Utility.extraerUsuario(request)
      val aapmedidormarcanuevo = new AapModelo(Some(0),
                                   aapmedidormarca.aamo_descripcion,
                                   aapmedidormarca.aamo_estado,
                                   usua_id.get)
      aamoService.crear(aapmedidormarcanuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAapModelo() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmedidormarca = json.as[AapModelo]
      val usua_id = Utility.extraerUsuario(request)
      val aapmedidormarcanuevo = new AapModelo(
        aapmedidormarca.aamo_id,
        aapmedidormarca.aamo_descripcion,
        aapmedidormarca.aamo_estado,
        usua_id.get
      )
      if (aamoService.actualizar(aapmedidormarcanuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAapModelo(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (aamoService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}