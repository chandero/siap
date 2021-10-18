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
class UnidadController @Inject()(
    unidadService: UnidadRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = unidadService.cuenta()
      unidadService.todos(page_size, current_page).map { unidades =>
        Ok(Json.obj("unidades" -> unidades, "total" -> total))
      }
    }

  def unidades(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    unidadService.unidades().map { unidades =>
      Ok(Json.toJson(unidades))
    }
  } 

  def buscarPorId(unid_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val unidad = unidadService.buscarPorId(unid_id)
      unidad match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarUnidad() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var unidad = json.as[Unidad]
      val usua_id = Utility.extraerUsuario(request)
      val unidadnuevo = new Unidad(Some(0),
                                   unidad.unid_descripcion,
                                   unidad.unid_abreviatura,
                                   unidad.unid_tipo,
                                   unidad.unid_estado,
                                   usua_id.get)
      unidadService.crear(unidadnuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarUnidad() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var unidad = json.as[Unidad]
      val usua_id = Utility.extraerUsuario(request)
      val unidadnuevo = new Unidad(
        unidad.unid_id,
        unidad.unid_descripcion,
        unidad.unid_abreviatura,
        unidad.unid_tipo,
        unidad.unid_estado,
        usua_id.get
      )
      if (unidadService.actualizar(unidadnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarUnidad(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (unidadService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
