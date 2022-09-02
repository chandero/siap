package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import scala.collection.mutable.MutableList

import pdi.jwt.JwtSession

import utilities._

import dto.ResultDto
import dto.CuadrillaDto

@Singleton
class CuadrillaController @Inject()(
    cuadrillaService: CuadrillaRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  def todos(page_size: Long, current_page: Long) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val total = cuadrillaService.cuenta(empr_id.get)      
      cuadrillaService.todos(empr_id.get, page_size, current_page).map { cuadrillas =>
        Ok(Json.obj("cuadrillas" -> cuadrillas, "total" -> total))
      }
    }

  def cuadrillas() =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      cuadrillaService.cuadrillas(empr_id.get).map { cuadrilla =>
        Ok(Json.toJson(cuadrilla))
      }
    }    

  def buscarPorId(cuad_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val cuadrilla = cuadrillaService.buscarPorId(cuad_id)
      cuadrilla match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(cuadrilla) => {
          var lista = MutableList[Long]()
          cuadrilla.usuarios.foreach { usuario =>
              lista += usuario.usua_id.get
          }
          var cuadrilladto = new CuadrillaDto(cuadrilla.cuad_id, 
                                              cuadrilla.cuad_descripcion, 
                                              cuadrilla.cuad_estado, 
                                              cuadrilla.usua_id, 
                                              lista.toList)

          Future.successful(Ok(Json.toJson(cuadrilladto)))
        }
      }

  }

  def guardarCuadrilla() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var cuadrilla = json.as[CuadrillaDto]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val cuadrillanuevo = new Cuadrilla(Some(0),
                                   cuadrilla.cuad_descripcion,
                                   cuadrilla.cuad_estado,
                                   usua_id.get,
                                   empr_id.get,
                                   List()
                                   )
      cuadrillaService.crear(cuadrillanuevo).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarCuadrilla() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var cuadrilla = json.as[CuadrillaDto]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      var lista = MutableList[Usuario]()
      cuadrilla.usuarios.foreach { id =>
        val usuario = new Usuario(Some(id), "", Some(""), "", "", true, None, None)
        lista += usuario
      }
      val cuadrillanuevo = new Cuadrilla(
        cuadrilla.cuad_id,
        cuadrilla.cuad_descripcion,
        cuadrilla.cuad_estado,
        usua_id.get,
        empr_id.get,
        lista.toList
      )
      if (cuadrillaService.actualizar(cuadrillanuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarCuadrilla(cuad_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      if (cuadrillaService.borrar(empr_id.get, cuad_id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }
}
