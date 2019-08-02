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
class ElementoController @Inject()(
    elementoService: ElementoRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(filter: String, page_size: Long, current_page: Long): Action[AnyContent] =
    authenticatedUserAction.async { implicit request: Request[AnyContent] => 
      val empr_id = Utility.extraerEmpresa(request)
      val filtro = filter.split(":")
      var filtrado = ""
      if (filtro.length > 1) {
        filtrado = filtro(1)
      } else {
        filtrado = ""
      }
      val total = elementoService.cuenta(filtrado, empr_id.get)
      elementoService.todos(filtrado, page_size, current_page, empr_id.get).map { elementos =>
        Ok(Json.obj("elementos" -> elementos, "total" -> total))
      }
    }

  def elementos(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    elementoService.elementos(empr_id.get).map { elementos =>
      Ok(Json.toJson(elementos))
    }
  } 

  def buscarPorId(elem_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val elemento = elementoService.buscarPorId(elem_id)
      elemento match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(elemento) => {
          Future.successful(Ok(Json.toJson(elemento)))
        }
      }

  }

  def buscarPorDescripcion(query: String) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      elementoService.buscarPorDescripcion(query+"%", empr_id.get).map { elementos =>
        Ok(Json.toJson(elementos))
      }
  }  

  def guardarElemento() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var elemento = json.as[Elemento]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val elementonuevo = new Elemento(Some(0),
                                   elemento.elem_descripcion,
                                   elemento.elem_codigo,
                                   elemento.elem_ucap,
                                   elemento.elem_estado,
                                   elemento.tiel_id,
                                   empr_id,
                                   usua_id,
                                   elemento.ucap_id,
                                   elemento.caracteristicas)
      elementoService.crear(elementonuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarElemento() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var elemento = json.as[Elemento]
      val usua_id = Utility.extraerUsuario(request)
      val elementonuevo = new Elemento(
        elemento.elem_id,
        elemento.elem_descripcion,
        elemento.elem_codigo,
        elemento.elem_ucap,
        elemento.elem_estado,
        elemento.tiel_id,
        elemento.empr_id,
        usua_id,
        elemento.ucap_id,
        elemento.caracteristicas
      )
      if (elementoService.actualizar(elementonuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarElemento(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (elementoService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
