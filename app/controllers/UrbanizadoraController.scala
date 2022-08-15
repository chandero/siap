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
class UrbanizadoraController @Inject()(
    urbaService: UrbanizadoraRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = urbaService.cuenta()
      urbaService.todos(page_size, current_page).map { urbaes =>
        Ok(Json.obj("urbanizadoras" -> urbaes, "total" -> total))
      }
    }

  def urbanizadoras(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    urbaService.urbanizadoras().map { urbas =>
      Ok(Json.toJson(urbas))
    }
  } 

  def buscarPorId(urba_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val urba = urbaService.buscarPorId(urba_id)
      urba match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(urba) => {
          Future.successful(Ok(Json.toJson(urba)))
        }
      }

  }

  def guardar() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var urba = json.as[Urbanizadora]
      val usua_id = Utility.extraerUsuario(request)
      val urbanuevo = new Urbanizadora(Some(0),
                                   urba.urba_descripcion,
                                   urba.urba_estado,
                                   usua_id)
      urbaService.crear(urbanuevo).map { result =>
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
      var urba = json.as[Urbanizadora]
      val usua_id = Utility.extraerUsuario(request)
      val urbanuevo = new Urbanizadora(
        urba.urba_id,
        urba.urba_descripcion,
        urba.urba_estado,
        usua_id
      )
      if (urbaService.actualizar(urbanuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrar(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (urbaService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
