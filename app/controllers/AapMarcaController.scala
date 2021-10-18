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
class AapMarcaController @Inject()(
    aamaService: AapMarcaRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async {
      val total = aamaService.cuenta()
      aamaService.todos(page_size, current_page).map { aapmarcas =>
        Ok(Json.obj("aapmarcas" -> aapmarcas, "total" -> total))
      }
    }

  def aapmarcas(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    aamaService.aapmarcas().map { aapmarcas =>
      Ok(Json.toJson(aapmarcas))
    }
  } 

  def buscarPorId(aama_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val aapmarca = aamaService.buscarPorId(aama_id)
      aapmarca match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(empresa) => {
          Future.successful(Ok(Json.toJson(empresa)))
        }
      }

  }

  def guardarAapMarca() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmarca = json.as[AapMarca]
      val usua_id = Utility.extraerUsuario(request)
      val aapmarcanuevo = new AapMarca(Some(0),
                                   aapmarca.aama_descripcion,
                                   aapmarca.aama_estado,
                                   usua_id.get)
      aamaService.crear(aapmarcanuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAapMarca() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var aapmarca = json.as[AapMarca]
      val usua_id = Utility.extraerUsuario(request)
      val aapmarcanuevo = new AapMarca(
        aapmarca.aama_id,
        aapmarca.aama_descripcion,
        aapmarca.aama_estado,
        usua_id.get
      )
      if (aamaService.actualizar(aapmarcanuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAapMarca(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (aamaService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}