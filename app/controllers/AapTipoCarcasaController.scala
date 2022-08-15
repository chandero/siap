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
class AapTipoCarcasaController @Inject()(
    aatcService: AapTipoCarcasaRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = aatcService.cuenta()
      aatcService.todos(page_size, current_page).map { aaptiposcarcasa =>
        Ok(Json.obj("aaptiposcarcasa" -> aaptiposcarcasa, "total" -> total))
      }
    }

  def aaptiposcarcasa(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    aatcService.aaptiposcarcasa().map { aaptiposcarcasa =>
      Ok(Json.toJson(aaptiposcarcasa))
    }
  } 

  def buscarPorId(aatc_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val aapmedidormarca = aatcService.buscarPorId(aatc_id)
      aapmedidormarca match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarAapTipoCarcasa() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmedidormarca = json.as[AapTipoCarcasa]
      val usua_id = Utility.extraerUsuario(request)
      val aapmedidormarcanuevo = new AapTipoCarcasa(Some(0),
                                   aapmedidormarca.aatc_descripcion,
                                   aapmedidormarca.aatc_estado,
                                   usua_id.get)
      aatcService.crear(aapmedidormarcanuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAapTipoCarcasa() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmedidormarca = json.as[AapTipoCarcasa]
      val usua_id = Utility.extraerUsuario(request)
      val aapmedidormarcanuevo = new AapTipoCarcasa(
        aapmedidormarca.aatc_id,
        aapmedidormarca.aatc_descripcion,
        aapmedidormarca.aatc_estado,
        usua_id.get
      )
      if (aatcService.actualizar(aapmedidormarcanuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAapTipoCarcasa(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (aatcService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}