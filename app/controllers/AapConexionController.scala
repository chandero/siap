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
class AapConexionController @Inject()(
    aacoService: AapConexionRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = aacoService.cuenta()
      aacoService.todos(page_size, current_page).map { aapconexiones =>
        Ok(Json.obj("aapconexions" -> aapconexiones, "total" -> total))
      }
    }

  def aapconexiones(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    aacoService.aapconexiones().map { aapconexiones =>
      Ok(Json.toJson(aapconexiones))
    }
  } 

  def buscarPorId(aaco_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val aapconexion = aacoService.buscarPorId(aaco_id)
      aapconexion match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarAapConexion() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapconexion = json.as[AapConexion]
      val usua_id = Utility.extraerUsuario(request)
      val aapconexionnuevo = new AapConexion(Some(0),
                                   aapconexion.aaco_descripcion,
                                   aapconexion.aaco_estado,
                                   usua_id.get)
      aacoService.crear(aapconexionnuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAapConexion() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapconexion = json.as[AapConexion]
      val usua_id = Utility.extraerUsuario(request)
      val aapconexionnuevo = new AapConexion(
        aapconexion.aaco_id,
        aapconexion.aaco_descripcion,
        aapconexion.aaco_estado,
        usua_id.get
      )
      if (aacoService.actualizar(aapconexionnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAapConexion(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (aacoService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}