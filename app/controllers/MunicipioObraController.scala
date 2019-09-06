package controllers

import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}
import java.io.{OutputStream, ByteArrayOutputStream}
import org.joda.time.LocalDate

import pdi.jwt.JwtSession

import utilities._

import dto._

@Singleton
class MunicipioObraController @Inject()(
    motService: MunicipioObraRepository,
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
    var filtro = Utility.procesarFiltrado(filter)
    if (filtro == "()") {
      filtro = ""
    }
    val empr_id = Utility.extraerEmpresa(request)
    val total = motService.cuenta(empr_id.get)
    motService.todos(page_size, current_page, empr_id.get, orderby, filtro).map { mots =>
        Ok(Json.obj("mots" -> mots, "total" -> total))
      }
    }

  def muobs(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    motService.muobs(empr_id.get).map { mots =>
      Ok(Json.toJson(mots))
    }
  }

  def buscarPorId(muot_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val mot = motService.buscarPorId(muot_id)
      mot match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(mot) => {
          Future.successful(Ok(Json.toJson(mot)))
        }
      }
  }

  def buscarPorConsecutivo(muot_consecutivo: scala.Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val mot = motService.buscarPorConsecutivo(muot_consecutivo, empr_id.get)
      mot match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(mot) => {
          Future.successful(Ok(Json.toJson(mot)))
        }
      }
  }

  def guardar() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var mot = json.as[MunicipioObra]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val motnuevo = new MunicipioObra(null,
                                                empr_id,
                                                mot.muob_consecutivo,
                                                mot.muob_descripcion,
                                                mot.muob_reportetecnico,
                                                mot.muob_estado,
                                                usua_id,
                                                mot.muob_fecharecepcion,
                                                mot.muob_radicado,
                                                mot.muob_fechaentrega,
                                                mot.muob_direccion,
                                                mot.barr_id
                                              )
      motService.crear(motnuevo).map { id =>
        if (id > 0) {
          Created(Json.obj("id" ->id))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizar() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var mot = json.as[MunicipioObra]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val motnuevo = new MunicipioObra(mot.muob_id,
                                                empr_id,
                                                mot.muob_consecutivo,
                                                mot.muob_descripcion,
                                                mot.muob_reportetecnico,
                                                mot.muob_estado,
                                                usua_id,
                                                mot.muob_fecharecepcion,
                                                mot.muob_radicado,
                                                mot.muob_fechaentrega,
                                                mot.muob_direccion,
                                                mot.barr_id
                                              )
      if (motService.actualizar(motnuevo)) {
          Future.successful(Ok(Json.toJson("true")))
      } else {
          Future.successful(NotAcceptable(Json.toJson("false")))
      }
  }  

  def borrar(muob_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      if (motService.borrar(muob_id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }  
}