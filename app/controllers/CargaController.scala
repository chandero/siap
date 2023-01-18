package controllers

import javax.inject.Inject
import java.time.Instant
import java.util.UUID
import java.nio.file.Paths
import java.io.File

import org.joda.time.DateTime
import models._
import dto._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton

import pdi.jwt.JwtSession
import play.api.Configuration

import scala.concurrent.{ExecutionContext, Future}
import scala.collection.mutable.ListBuffer

import utilities._

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import models.MedidorRepository

@Singleton
class CargaController @Inject()(
    service: CargaRepository,
    mService: MedidorRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction,
    config: Configuration)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {

    implicit val formats = Serialization.formats(NoTypeHints) ++ List(DateTimeSerializer)  
   
    def procesar_xlsx(anho: Int, mes: Int, tipo: Int, uuid: String) = authenticatedUserAction.async { implicit request =>
        val empr_id = Utility.extraerEmpresa(request)
        val usua_id = Utility.extraerUsuario(request)
        val thread = new Thread {
            override def run {
                tipo match {
                    case 1 => mService.cargaLectura(anho, mes, empr_id.get, usua_id.get, uuid)
                }
                
            }
        }
        thread.start
        Future.successful(Ok("true"))
    }

    def ultimaCarga() = authenticatedUserAction { implicit request =>
        val empr_id = Utility.extraerEmpresa(request)
        val ultimaCarga = service.ultimaCarga(empr_id.get)
        Ok(write(ultimaCarga))
    }

    def eliminar(anho: Int, mes: Int, tipo: Int) = authenticatedUserAction.async { implicit request =>
        val empr_id = Utility.extraerEmpresa(request)
        val usua_id = Utility.extraerUsuario(request)
        val result =  tipo match {
                    case 1 => service.eliminar_1(anho, mes, empr_id.get, usua_id.get)
                }
        Future.successful(Ok(write(result)))
    }

    def todos() = authenticatedUserAction.async { implicit request =>
        val json = request.body.asJson.get
        val page_size = ( json \ "page_size").as[Long]
        val current_page = ( json \ "current_page").as[Long]
        val orderby = ( json \ "orderby").as[String]
        val filter = ( json \ "filter").as[QueryDto]
        val tipo = ( json \ "tipo").as[Int]
        val filtro_a = Utility.procesarFiltrado(filter)
        var filtro = filtro_a.replace("\"", "'")
        if (filtro == "()") {
            filtro = ""
        }        
        val empr_id = Utility.extraerEmpresa(request);
        val total = service.cuenta(empr_id.get, tipo)
        service.todos(empr_id.get, tipo, page_size, current_page).map { carga =>
            Ok(write(new ResultDto[ControlCarga](carga.toList, total)))
        }
    }
   
}