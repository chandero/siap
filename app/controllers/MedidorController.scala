package controllers

import javax.inject.Inject
import java.nio.file.Paths

import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

import pdi.jwt.JwtSession

import utilities._

@Singleton
class MedidorController @Inject()(mService: MedidorRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction, config: Configuration)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def buscarPorId(medi_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val m = mService.buscarPorId(medi_id)
      m match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(m) => {
          Future.successful(Ok(Json.toJson(m)))
        }
      }             
    }

    def todos(p:Long, c:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        mService.todos(p, c, empr_id.get).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def medidores() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        mService.medidores(empr_id.get).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def medidor_tabla_dato() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      mService.medidor_tabla_dato().map { result =>
         Ok(Json.toJson(result))
      }
    }

    def guardar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var m = json.as[Medidor]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val mnuevo = new Medidor(Some(0),m.medi_numero, m.amem_id, m.amet_id, m.aacu_id, empr_id, usua_id, m.medi_direccion, m.medi_estado, m.medi_acta, m.datos)
      mService.crear(mnuevo).map { result =>
        if (result > 0){
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
    }

    def actualizar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var m = json.as[Medidor]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)      
      val mnuevo = new Medidor(m.medi_id,m.medi_numero, m.amem_id, m.amet_id, m.aacu_id, empr_id, usua_id, m.medi_direccion, m.medi_estado, m.medi_acta, m.datos)
      if (mService.actualizar(mnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }    

    def borrar(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        val usua_id = Utility.extraerUsuario(request)
        if (mService.borrar(id, usua_id.get)) {
            Future.successful(Ok(Json.toJson("true")))
        } else {
            Future.successful(ServiceUnavailable(Json.toJson("false")))
        }
    }

    def informe_siap_medidor(empr_id: scala.Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      mService.informe_siap_medidor(empr_id).map { lista => 
        Ok(Json.toJson(lista))
      }
    }

    def upload = Action(parse.multipartFormData) { request =>
      request.body
        .file("picture")
        .map { picture =>
          // only get the last part of the filename
          // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
          val ubicacion = config.get[String]("tmp.ubicacion")
          val filename = Paths.get(picture.filename).getFileName
          picture.ref.moveTo(Paths.get(ubicacion + filename), replace = true)
          Ok("Archivo Cargado")
        }
        .getOrElse {
          Ok("No se pudo cargar el archivo!")
        }
    }    
}