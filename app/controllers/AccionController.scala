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
class AccionController @Inject()(
    acciService: AccionRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = acciService.cuenta()
      acciService.todos(page_size, current_page).map { acciones =>
        Ok(Json.obj("acciones" -> acciones, "total" -> total))
      }
    }

  def acciones(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    acciService.acciones().map { acciones =>
      Ok(Json.toJson(acciones))
    }
  } 

  def buscarPorId(acci_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val accion = acciService.buscarPorId(acci_id)
      accion match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarAccion() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var accion = json.as[Accion]
      val usua_id = Utility.extraerUsuario(request)
      val accionnuevo = new Accion(Some(0),
                                   accion.acci_descripcion,
                                   accion.acci_enaap,
                                   accion.acci_estado,
                                   usua_id.get)
      acciService.crear(accionnuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAccion() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var accion = json.as[Accion]
      val usua_id = Utility.extraerUsuario(request)
      val accionnuevo = new Accion(
        accion.acci_id,
        accion.acci_descripcion,
        accion.acci_enaap,
        accion.acci_estado,
        usua_id.get
      )
      if (acciService.actualizar(accionnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAccion(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (acciService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
