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
class SolicitudTipoController @Inject()(
    sService: SolicitudTipoRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = sService.cuenta()
      sService.todos(page_size, current_page).map { solicitudtipos =>
        Ok(Json.obj("solicitudtipos" -> solicitudtipos, "total" -> total))
      }
    }

  def solicitudtipos(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    sService.solicitudtipos().map { solicitudtipos =>
      Ok(Json.toJson(solicitudtipos))
    }
  } 

  def buscarPorId(soti_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val st = sService.buscarPorId(soti_id)
      st match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(st) => {
          Future.successful(Ok(Json.toJson(st)))
        }
      }

  }

  def guardar() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var st = json.as[SolicitudTipo]
      val usua_id = Utility.extraerUsuario(request)
      val stnuevo = new SolicitudTipo(Some(0),
                                   st.soti_descripcion,
                                   st.soti_estado,
                                   usua_id.get)
      sService.crear(stnuevo).map { result =>
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
      var st = json.as[SolicitudTipo]
      val usua_id = Utility.extraerUsuario(request)
      val stnuevo = new SolicitudTipo(
        st.soti_id,
        st.soti_descripcion,
        st.soti_estado,
        usua_id.get
      )
      if (sService.actualizar(stnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrar(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (sService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
