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
import dto.QueryDto

@Singleton
class ElementoController @Inject()(
    elementoService: ElementoRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(): Action[AnyContent] =
    authenticatedUserAction.async { implicit request: Request[AnyContent] => 
      val json = request.body.asJson.get
      val page_size = ( json \ "page_size").as[Long]
      val current_page = ( json \ "current_page").as[Long]
      val orderby = ( json \ "orderby").as[String]
      val filter = ( json \ "filter").as[QueryDto]
      val filtro_a = Utility.procesarFiltrado(filter)
      var filtro = filtro_a.replace("\"", "'")
      if (filtro == "()") {
        filtro = ""
      }
      val empr_id = Utility.extraerEmpresa(request)    
      val total = elementoService.cuenta(empr_id.get, filtro)
      elementoService.todos(empr_id.get, page_size, current_page, orderby, filtro).map { elementos =>
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

  def buscarPorCodigo(elem_codigo: String) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] => 
      var elemento = elementoService.buscarPorCodigo(elem_codigo)
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
                                   elemento.caracteristicas,
                                   elemento.precio)
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
        elemento.caracteristicas,
        elemento.precio
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

  def actualizarPrecio = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      val elem_id = (json \ "elem_id").as[Long]
      val elpr_anho = (json \ "elpr_anho").as[Int]
      val elpr_precio = (json \ "elpr_precio").as[BigDecimal]
      val usua_id = Utility.extraerUsuario(request)
      if (elementoService.actualizarPrecio(elem_id, elpr_anho, elpr_precio, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }  
}
