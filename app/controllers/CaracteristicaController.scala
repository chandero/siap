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
class CaracteristicaController @Inject()(
    caracteristicaService: CaracteristicaRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long) =
    authenticatedUserAction.async {
      val total = caracteristicaService.cuenta()
      caracteristicaService.todos(page_size, current_page).map { caracteristicas =>
        Ok(Json.obj("caracteristicas" -> caracteristicas, "total" -> total))
      }
    }

  def caracteristicas() =
    authenticatedUserAction.async {
      val total = caracteristicaService.cuenta()
      caracteristicaService.caracteristicas().map { caracteristicas =>
        Ok(Json.toJson(caracteristicas))
      }
    }    

  def buscarPorId(cara_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val caracteristica = caracteristicaService.buscarPorId(cara_id)
      caracteristica match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(caracteristica) => {
          Future.successful(Ok(Json.toJson(caracteristica)))
        }
      }

  }

  def guardarCaracteristica() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var caracteristica = json.as[Caracteristica]
      val usua_id = Utility.extraerUsuario(request)
      val caracteristicanuevo = new Caracteristica(Some(0),
                                   caracteristica.cara_descripcion,
                                   caracteristica.cara_estado,
                                   caracteristica.cara_valores,
                                   caracteristica.unid_id,
                                   usua_id.get
                                   )
      caracteristicaService.crear(caracteristicanuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarCaracteristica() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var caracteristica = json.as[Caracteristica]
      val usua_id = Utility.extraerUsuario(request)
      val caracteristicanuevo = new Caracteristica(
        caracteristica.cara_id,
        caracteristica.cara_descripcion,
        caracteristica.cara_estado,
        caracteristica.cara_valores,
        caracteristica.unid_id,
        usua_id.get
      )
      if (caracteristicaService.actualizar(caracteristicanuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarCaracteristica(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (caracteristicaService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
