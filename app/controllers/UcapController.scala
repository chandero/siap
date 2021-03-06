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
class UcapController @Inject()(
    ucapService: UcapRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = ucapService.cuenta()
      ucapService.todos(page_size, current_page).map { ucapes =>
        Ok(Json.obj("ucapes" -> ucapes, "total" -> total))
      }
    }

  def ucaps(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    ucapService.ucaps().map { ucaps =>
      Ok(Json.toJson(ucaps))
    }
  } 

  def buscarPorId(ucap_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val ucap = ucapService.buscarPorId(ucap_id)
      ucap match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarUcap() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var ucap = json.as[Ucap]
      val usua_id = Utility.extraerUsuario(request)
      val ucapnuevo = new Ucap(Some(0),
                                   ucap.ucap_descripcion,
                                   ucap.ucap_estado,
                                   usua_id)
      ucapService.crear(ucapnuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarUcap() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var ucap = json.as[Ucap]
      val usua_id = Utility.extraerUsuario(request)
      val ucapnuevo = new Ucap(
        ucap.ucap_id,
        ucap.ucap_descripcion,
        ucap.ucap_estado,
        usua_id
      )
      if (ucapService.actualizar(ucapnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarUcap(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (ucapService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
