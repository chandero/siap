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
import dto.QueryDto
import dto.ResultDto
@Singleton
class CobroFacturaController @Inject()(
    sService: CobroFacturaRepository,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction,
    cc: ControllerComponents
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )

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
      val total = sService.cuenta(empr_id.get, filtro)
      sService.todos(empr_id.get, page_size, current_page, orderby, filtro).map { facturas =>
        Ok(write(ResultDto[cobro_factura](facturas.toList, total)))
      }
    }  

  def ordenSinFactura() = authenticatedUserAction.async { implicit request: Request[AnyContent] => 
    val empr_id = Utility.extraerEmpresa(request)
    sService.ordenSinFactura(empr_id.get).map { ordenes =>
      Ok(write(ordenes))
    }  
  }

  def buscarPorNumero(cofa_factura: Long) = 
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      sService.buscarPorNumero(cofa_factura, empr_id.get).map { result =>
        Ok(write(result))
      }
    }

    def crear() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val json = request.body.asJson.get
      var cobro = net.liftweb.json.parse((json).toString).extract[cobro_factura]
      val _idx = sService.crear(cobro, usua_id.get, empr_id.get)
      Future.successful(Ok(write(_idx)))
    }

    def actualizar() = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val json = request.body.asJson.get
      var cobro = net.liftweb.json.parse((json).toString).extract[cobro_factura]
      val _result = sService.actualizar(cobro, usua_id.get, empr_id.get)
      Future.successful(Ok(write(_result)))
    }

    def borrar(cofa_id: Long) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val _result = sService.borrar(cofa_id, usua_id.get, empr_id.get)
      Future.successful(Ok(write(_result)))
    }
}