package controllers

import javax.inject.Inject
import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration

import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import org.joda.time.DateTime

import pdi.jwt.JwtSession

import utilities._

import dto._

@Singleton
class AapController @Inject()(
    aapService: AapRepository,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )

  def todos() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      val page_size = (json \ "page_size").as[Long]
      val current_page = (json \ "current_page").as[Long]
      val orderby = (json \ "orderby").as[String]
      val filter = (json \ "filter").as[QueryDto]
      val filtro_a = Utility.procesarFiltrado(filter)
      var filtro = filtro_a.replace("\"", "'")
      if (filtro == "()") {
        filtro = ""
      }
      val empr_id = Utility.extraerEmpresa(request)
      val total = aapService.cuenta(empr_id.get, filtro)
      aapService
        .todos(empr_id.get, page_size, current_page, orderby, filtro)
        .map { aaps =>
          Ok(Json.obj("aaps" -> aaps, "total" -> total))
        }
  }

  def todosEliminados() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      val page_size = (json \ "page_size").as[Long]
      val current_page = (json \ "current_page").as[Long]
      val orderby = (json \ "orderby").as[String]
      val filter = (json \ "filter").as[QueryDto]
      val filtro_a = Utility.procesarFiltrado(filter)
      var filtro = filtro_a.replace("\"", "'")
      if (filtro == "()") {
        filtro = ""
      }
      val empr_id = Utility.extraerEmpresa(request)
      val total = aapService.cuentaEliminados(empr_id.get, filtro)
      aapService
        .todosEliminados(empr_id.get, page_size, current_page, orderby, filtro)
        .map { aaps =>
          Ok(Json.obj("aaps" -> aaps, "total" -> total))
        }
  }

  def aaps() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      val filter = (json \ "filter").as[QueryDto]
      var filtro = Utility.procesarFiltrado(filter)
      if (filtro == "()") {
        filtro = ""
      }
      val empr_id = Utility.extraerEmpresa(request)
      aapService.aaps(empr_id.get, filtro).map { aaps =>
        Ok(Json.toJson(aaps))
      }
  }

  def aapsMobile() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      aapService.aapsMobile(empr_id.get).map { aaps =>
        Ok(write(aaps))
      }
  }

  def buscarSiguienteACrear() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val siguiente = aapService.buscarSiguienteACrear(empr_id.get)
      Future.successful(Ok(Json.toJson(siguiente)))
  }

  def buscarPorId(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val aap = aapService.buscarPorId(id, empr_id.get)
      aap match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(aap) => {
          Future.successful(Ok(Json.toJson(aap)))
        }
      }
  }

  def buscarParaVerificar(aap_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val result = aapService.buscarParaVerificar(aap_id, empr_id.get)
      Future.successful(Ok(write(result)))
  }

  def buscarParaEditar(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val activo = aapService.buscarParaEditar(id, empr_id.get)
      activo match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(activo) => {
          Future.successful(Ok(Json.toJson(activo)))
        }
      }
  }

  def buscarPorApoyo(id: String) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val aap = aapService.buscarPorApoyo(id, empr_id.get)
      aap match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(aap) => {
          Future.successful(Ok(Json.toJson(aap)))
        }
      }
  }

  def buscarPorMaterial(codigo: String, tiel_id: scala.Long) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      aapService.buscarPorMaterial(codigo, tiel_id, empr_id.get).map { aaps =>
        Ok(Json.toJson(aaps))
      }
    }

  def guardarAap() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var activo = json.as[Activo]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      aapService.crear(activo, empr_id.get, usua_id.get).map { result =>
        if (result > 0) {
          Created(Json.toJson("true"))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarAap() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var activo = json.as[Activo]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      if (aapService.actualizar(activo, empr_id.get, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarAap(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      if (aapService.borrar(id, usua_id.get, empr_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }

  def recuperarAap(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      if (aapService.recuperar(id, usua_id.get, empr_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }

  def buscarPorCodigoWeb(id: Long, empr_id: Long, token: String) =
    Action.async { implicit request: Request[AnyContent] =>
      val secret = config.get[String]("play.http.secret.key")
      if (secret == token) {
        val aap = aapService.buscarPorCodigoWeb(id, empr_id)
        aap match {
          case None => {
            Future.successful(NotFound(Json.toJson("false")))
          }
          case Some(aap) => {
            Future.successful(Ok(write(aap)))
          }
        }
      } else {
        Future.successful(NotFound)
      }
    }

}
