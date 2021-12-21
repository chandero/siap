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
class UnitarioController @Inject()(
    uService: UnitarioRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async { implicit request =>
      val empr_id = Utility.extraerEmpresa(request)
      val total = uService.cuenta(empr_id.get)
      uService.todos(empr_id.get, page_size, current_page).map { ucapes =>
        Ok(Json.obj("unitarios" -> ucapes, "total" -> total))
      }
    }

  def unitarios(): Action[AnyContent] = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    uService.unitarios(empr_id.get).map { unitarios =>
      Ok(Json.toJson(unitarios))
    }
  } 

  def buscarPorId(unit_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val unitario = uService.buscarPorId(unit_id)
      unitario match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(unitario) => {
          Future.successful(Ok(Json.toJson(unitario)))
        }
      }

  }

  def guardar() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var unitario = json.as[Unitario]
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val unuevo = new Unitario(Some(0),
                                   unitario.unit_codigo,
                                   unitario.unit_descripcion,
                                   empr_id)
      uService.crear(unuevo, usua_id.get).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizar() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var unitario = json.as[Unitario]
      val usua_id = Utility.extraerUsuario(request)
      val unuevo = new Unitario(
        unitario.unit_id,
        unitario.unit_codigo,
        unitario.unit_descripcion,
        unitario.empr_id
      )
      if (uService.actualizar(unuevo, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrar(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (uService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }

  def material() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      uService.material(empr_id.get).map { material =>
        Ok(Json.toJson(material))
      }
  }
}
