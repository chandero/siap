package controllers

import javax.inject.Inject
import java.util.Calendar

import org.joda.time.LocalDateTime

import models._
import dto.QueryDto

import play.api.mvc._
import play.api.libs.json._

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}

import pdi.jwt.JwtSession

import utilities._

@Singleton
class PosteController @Inject()(mService: PosteRepository, cc: ControllerComponents, authenticatedUserAction: AuthenticatedUserAction)(implicit ec: ExecutionContext) extends AbstractController(cc) {

    def buscarPorId(aap_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val m = mService.buscarPorId(aap_id, empr_id.get)
      m match {
        case None => {
            Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(m) => {
          Future.successful(Ok(Json.toJson(m)))
        }
      }             
    }

    def todos = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
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
      val total = mService.cuenta(empr_id.get, filtro)
      mService.todos(empr_id.get, page_size, current_page, orderby, filtro).map { aaps =>
        Ok(Json.obj("aaps" -> aaps, "total" -> total))
      }
    }

  def todosEliminados() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
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
    val total = mService.cuentaEliminados(empr_id.get, filtro)
    mService.todosEliminados(empr_id.get, page_size, current_page, orderby, filtro).map { aaps =>
      Ok(Json.obj("aaps" -> aaps, "total" -> total))
    }
  }   

  def postes() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
        val empr_id = Utility.extraerEmpresa(request)
        mService.postes(empr_id.get).map { result =>
           Ok(Json.toJson(result))
        }
    }

    def guardar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var m = json.as[Poste]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())      
      val mnuevo = new Poste(m.aap_id,empr_id, usua_id, m.aap_direccion, m.barr_id, m.esta_id, m.tipo_id, m.aap_poste_altura, m.aap_poste_propietario, Some(hora.toDateTime), None)
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
      var m = json.as[Poste]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val mnuevo = new Poste(m.aap_id, empr_id, usua_id, m.aap_direccion, m.barr_id, m.esta_id, m.tipo_id, m.aap_poste_altura, m.aap_poste_propietario, m.aap_fechacreacion, None)
      if (mService.actualizar(mnuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
    }    

    def borrar(id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
        val usua_id = Utility.extraerUsuario(request)
        val empr_id = Utility.extraerEmpresa(request)
        if (mService.borrar(id, empr_id.get, usua_id.get)) {
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
        Future.successful(Ok(Json.toJson(activo)))
      }
    }
  }

  def buscarSiguienteACrear() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    val siguiente = mService.buscarSiguienteACrear(empr_id.get)
    Future.successful(Ok(Json.toJson(siguiente)))
  }  
    
  def informe_siap_poste(empr_id: scala.Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
     mService.informe_siap_poste(empr_id).map { lista => 
       Ok(Json.toJson(lista))
     }
  }

}