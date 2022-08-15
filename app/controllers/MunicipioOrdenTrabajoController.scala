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
class MunicipioOrdenTrabajoController @Inject()(
    motService: MunicipioOrdenTrabajoRepository,
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

  def mots(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    motService.mots(empr_id.get).map { mots =>
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
      var mot = json.as[MunicipioOrdenTrabajo]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val motnuevo = new MunicipioOrdenTrabajo(null,
                                                empr_id,
                                                mot.muot_consecutivo,
                                                mot.muot_descripcion,
                                                mot.muot_asunto,
                                                mot.muot_remitente,
                                                mot.muot_recibe,
                                                mot.muot_reportetecnico,
                                                mot.muot_estado,
                                                usua_id,
                                                mot.muot_fecharecepcion,
                                                mot.muot_radicado,
                                                mot.muot_fechaejecucion
                                              )
      motService.crear(motnuevo).map { case (id, consec) =>
        if (id > 0) {
          Created(Json.obj("id" ->id, "consec" -> consec))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }
}