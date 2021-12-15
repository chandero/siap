package controllers

import javax.inject.Inject

import models._

import play.api.mvc._
import play.api.libs.json._

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

import pdi.jwt.JwtSession

import utilities._

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

@Singleton
class TransformadorController @Inject()(mService: TransformadorRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )  
    def buscarPorId(aap_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val m = mService.buscarPorId(aap_id, empr_id.get)
      m match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(m) => {
          Future.successful(Ok(write(m)))
        }
      }             
    }

    def todos(p:Long, c:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        mService.todos(p, c, empr_id.get).map { result =>
           Ok(write(result))
        }
    }

    def transformadores() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        mService.transformadores(empr_id.get).map { result =>
           Ok(write(result))
        }
    }

    def guardar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var m = net.liftweb.json.parse((json).toString).extract[Transformador]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val mnuevo = new Transformador(
          Some(0), 
          m.aap_numero,
          empr_id,
          usua_id,
          m.barr_id,
          m.aap_direccion,
          m.aap_codigo_apoyo,
          m.aap_propietario,
          m.aap_marca,
          m.aap_serial,
          m.aap_kva,
          m.tipo_id,
          m.aap_fases,
          m.aap_tension_p,
          m.aap_tension_s,
          m.aap_referencia,
          m.aap_estado
          )
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
      var m = net.liftweb.json.parse((json).toString).extract[Transformador]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val mnuevo = new Transformador(
          m.aap_id,
          m.aap_numero,
          m.empr_id,
          usua_id,
          m.barr_id,
          m.aap_direccion,
          m.aap_codigo_apoyo,
          m.aap_propietario,
          m.aap_marca,
          m.aap_serial,
          m.aap_kva,
          m.tipo_id,
          m.aap_fases,
          m.aap_tension_p,
          m.aap_tension_s,
          m.aap_referencia,
          m.aap_estado
          )
      val result = mService.actualizar(mnuevo)
      if (result == true) {
        Future.successful(Created(Json.toJson("true")))
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

  def buscarParaVerificar(aap_id:Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    val result = mService.buscarParaVerificar(aap_id, empr_id.get)
    Future.successful(Ok(Json.toJson(result)))
  }
  
  def buscarParaEditar(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    val activo = mService.buscarParaEditar(id, empr_id.get)
    activo match {
      case None => {
        Future.successful(NotFound(Json.toJson("false")))
      }
      case Some(activo) => {
        Future.successful(Ok(write(activo)))
      }
    }
  }

  def informe_siap_transformador(empr_id: scala.Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    mService.informe_siap_transformador(empr_id).map { lista => 
      Ok(write(lista))
    }
  }
}