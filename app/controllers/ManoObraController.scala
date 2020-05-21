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
class ManoObraController @Inject()(
    moService: ManoObraRepository,
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
      val total = moService.cuenta(empr_id.get, filtro)
      moService.todos(empr_id.get, page_size, current_page, orderby, filtro).map { manoobras =>
        Ok(Json.obj("manoobras" -> manoobras, "total" -> total))
      }
    }

  def manoobras(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    moService.manoobras(empr_id.get).map { manoobras =>
      Ok(Json.toJson(manoobras))
    }
  } 

  def buscarPorId(maob_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val mo = moService.buscarPorId(maob_id)
      mo match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(mo) => {
          Future.successful(Ok(Json.toJson(mo)))
        }
      }
  }

  def buscarPorDescripcion(query: String) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      moService.buscarPorDescripcion("%"+query+"%", empr_id.get).map { manoobras =>
        Ok(Json.toJson(manoobras))
      }
  }  

  def guardar() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var mo = json.as[ManoObra]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val monuevo = new ManoObra(Some(0),
                                   mo.maob_tipo,
                                   mo.maob_descripcion,
                                   mo.maob_estado,
                                   empr_id,
                                   usua_id)
      moService.crear(monuevo).map { result =>
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
      var mo = json.as[ManoObra]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)      
      val monuevo = new ManoObra(  mo.maob_id,
                                   mo.maob_tipo,
                                   mo.maob_descripcion,
                                   mo.maob_estado,
                                   empr_id,
                                   usua_id)
      if (moService.actualizar(monuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrar(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (moService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
