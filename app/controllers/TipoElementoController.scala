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
class TipoElementoController @Inject()(
    tielService: TipoElementoRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = tielService.cuenta()
      tielService.todos(page_size, current_page).map { tiposelemento =>
        Ok(Json.obj("tiposelemento" -> tiposelemento, "total" -> total))
      }
    }

  def tiposelemento(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    tielService.tiposelemento().map { tiposelemento =>
      Ok(Json.toJson(tiposelemento))
    }
  } 

  def buscarPorId(tiel_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val tipoelemento = tielService.buscarPorId(tiel_id)
      tipoelemento match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarTipoElemento() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tipoelemento = json.as[TipoElemento]
      val usua_id = Utility.extraerUsuario(request)
      val tipoelementonuevo = new TipoElemento(Some(0),
                                   tipoelemento.tiel_descripcion,
                                   tipoelemento.tiel_estado,
                                   usua_id.get)
      tielService.crear(tipoelementonuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarTipoElemento() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var tipoelemento = json.as[TipoElemento]
      val usua_id = Utility.extraerUsuario(request)
      val tipoelementonuevo = new TipoElemento(
        tipoelemento.tiel_id,
        tipoelemento.tiel_descripcion,
        tipoelemento.tiel_estado,
        usua_id.get
      )
      if (tielService.actualizar(tipoelementonuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarTipoElemento(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (tielService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
