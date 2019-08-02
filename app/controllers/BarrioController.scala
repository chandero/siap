package controllers

import javax.inject.Inject

import models._

import play.api.mvc._
import play.api.libs.json._

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

import pdi.jwt.JwtSession

import utilities._

@Singleton
class BarrioController @Inject()(barrioService: BarrioRepository, tipobarrioService: TipoBarrioRepository, empresaService: EmpresaRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {
    def buscarpormunicipio(muni_id: Long, page_size: Long, current_page: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        val total = barrioService.cuenta(muni_id)
        barrioService.todos(muni_id, page_size, current_page).map { barrios => 
            Ok(Json.obj("barrios" -> barrios, "total" -> total))
        }
    }

    def buscarporempresa() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        val empresa = empresaService.buscarPorId(empr_id.get)
        empresa match {
          case None => {
            Future.successful(NotFound(Json.toJson("false")))
          }
          case Some(empresa) => {
              barrioService.buscarPorMunicipio(empresa.muni_id).map { barrios => 
                  Ok(Json.toJson(barrios))
              }
          }
        }
    }    

    def buscarporid(barr_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val barrio = barrioService.buscarPorId(barr_id)
      barrio match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(barrio) => {
          Future.successful(Ok(Json.toJson(barrio)))
        }
      }             
    }

    def obtenertipobarrio() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        tipobarrioService.tiposbarrio().map { tiposbarrio => 
            Ok(Json.toJson(tiposbarrio))
        }
    }

    def guardarBarrio() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var barrio = json.as[Barrio]
      val usua_id = Utility.extraerUsuario(request)
      val barrionuevo = new Barrio(Some(0),barrio.barr_descripcion,barrio.barr_codigo,barrio.barr_estado,barrio.depa_id,barrio.muni_id,barrio.tiba_id,usua_id.get)
      barrioService.crear(barrionuevo).map { result =>
        if (result > 0){
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
    }

    def actualizarBarrio() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var barrio = json.as[Barrio]
      val usua_id = Utility.extraerUsuario(request)
      val barrionuevo = new Barrio(barrio.barr_id,barrio.barr_descripcion,barrio.barr_codigo,barrio.barr_estado,barrio.depa_id,barrio.muni_id,barrio.tiba_id,usua_id.get)
      if (barrioService.actualizar(barrionuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }    

    def borrarBarrio(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        if (barrioService.borrar(id)) {
            Future.successful(Ok(Json.toJson("true")))
        } else {
            Future.successful(ServiceUnavailable(Json.toJson("false")))
        }
    }
}