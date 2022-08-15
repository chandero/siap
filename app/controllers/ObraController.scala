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

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import pdi.jwt.JwtSession

import utilities._

import dto._

@Singleton
class ObraController @Inject()(
    obraService: ObraRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(DateTimeSerializer)                          

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
    val total = obraService.cuenta(empr_id.get)
    obraService.todos(page_size, current_page, empr_id.get, orderby, filtro).map { obras =>
        Ok(write(new ResultDto[Obra](obras.toList, total)))
      }
    }

  def obras(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    obraService.obras(empr_id.get).map { obras =>
      Ok(write(obras))
    }
  }

  def estados(): Action[AnyContent] = authenticatedUserAction.async {
    implicit request : Request[AnyContent] =>
    obraService.estados().map { estados =>
      Ok(Json.toJson(estados))
    }
  }

  def buscarPorId(obra_id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val obra = obraService.buscarPorId(obra_id)
      obra match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(obra) => {
          Future.successful(Ok(write(obra)))
        }
      }
  }

  def buscarPorConsecutivo(obra_consecutivo: scala.Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val obra = obraService.buscarPorConsecutivoConsulta(obra_consecutivo, empr_id.get)
      obra match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(obra) => {
          Future.successful(Ok(write(obra)))
        }
      }
  }

  def buscarPorRango(anho: Int, mes: Int) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      obraService.buscarPorRango(anho, mes, empr_id.get).map { obras =>
        Ok(write(obras))
      }
  }


  def guardarObra() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var obra = net.liftweb.json.parse(json.toString).extract[Obra]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val obranuevo = new Obra(null,
                            obra.obra_consecutivo,
                            obra.obra_nombre,
                            obra.obra_fecharecepcion,
                            obra.obra_direccion,
                            obra.obra_solicitante,
                            obra.obra_telefono,
                            obra.obra_email,
                            obra.obra_fechasolucion,
                            obra.obra_reportetecnico,
                            obra.obra_descripcion,
                            obra.obra_horainicio,
                            obra.obra_horafin,
                            obra.obra_modificado,
                            obra.ortr_id,
                            obra.muot_id,
                            obra.rees_id,
                            obra.orig_id,
                            obra.barr_id,
                            empr_id,
                            usua_id,
                            obra.meams,
                            obra.eventos
                 )
      obraService.crear(obranuevo).map { case (id, consec) =>
        if (id > 0) {
          Created(Json.obj("id" ->id, "consec" -> consec))
        } else {
          NotAcceptable(Json.toJson("true"))
        }
      }
  }

  def actualizarObra() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      var obra = net.liftweb.json.parse(json.toString).extract[Obra]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)      
      val obranuevo = new Obra(obra.obra_id,
                            obra.obra_consecutivo,
                            obra.obra_nombre,
                            obra.obra_fecharecepcion,
                            obra.obra_direccion,
                            obra.obra_solicitante,
                            obra.obra_telefono,
                            obra.obra_email,                            
                            obra.obra_fechasolucion,
                            obra.obra_reportetecnico,
                            obra.obra_descripcion,
                            obra.obra_horainicio,
                            obra.obra_horafin,
                            obra.obra_modificado,
                            obra.ortr_id,
                            obra.muot_id,
                            obra.rees_id,
                            obra.orig_id,
                            obra.barr_id,
                            empr_id,
                            usua_id,
                            obra.meams,
                            obra.eventos)
      if (obraService.actualizar(obranuevo)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(NotAcceptable(Json.toJson("true")))
      }
  }

  def borrarObra(id: Long) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val usua_id = Utility.extraerUsuario(request)
      if (obraService.borrar(id, usua_id.get)) {
        Future.successful(Ok(Json.toJson("true")))
      } else {
        Future.successful(ServiceUnavailable(Json.toJson("false")))
      }
  }

  def actualizarHistoria() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    if (obraService.actualizarHistoria(empr_id.get)) {
      Future.successful(Ok(Json.toJson("true"))) 
    } else {
      Future.successful(ServiceUnavailable(Json.toJson("false")))
    }
  }

  def validarCodigo(elem_id: scala.Long, codigo: String) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    val resultado = obraService.validarCodigo(elem_id, codigo, empr_id.get)
    Future.successful(Ok(Json.toJson(resultado)))
  }  

  def imprimirObra(id: Long, empr_id: Long) = Action {
     val os = obraService.imprimir(id, empr_id)
     Ok(os).as("application/pdf")
  }
   
  def imprimirRelacion(fecha_inicial: Long, fecha_final: Long, empr_id: Long, usua_id: Long) = Action {
     val os = obraService.imprimirRelacion(fecha_inicial, fecha_final, empr_id, usua_id)
     Ok(os).as("application/pdf")
  }

  def imprimirFormato(reti_id: Long, empr_id: Long) = Action {
     val os = obraService.formato(reti_id, empr_id)
     Ok(os).as("application/pdf")
  }  
}
