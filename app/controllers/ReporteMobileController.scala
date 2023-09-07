package controllers

import javax.inject.Inject
import java.io.{OutputStream, ByteArrayOutputStream, FileWriter}
import java.nio.file.Paths
import java.io.File
import java.io.FileReader

import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration
import play.filters.csrf._
import play.filters.csrf.CSRF.Token

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import com.google.inject.Singleton

import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import org.joda.time.LocalDate
import org.joda.time.DateTime
import org.joda.time.format.DateTimeFormat

import pdi.jwt.JwtSession

import utilities._

import dto._
import java.text.SimpleDateFormat

@Singleton
class ReporteMobileController @Inject()(
    reporteService: ReporteRepository,
    cc: ControllerComponents,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitMobileJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeMobileSerializer
  )

  def buscarPorConsecutivoMovil(reti_id: Long, repo_consecutivo: Int) =
    authenticatedUserAction.async { implicit request: Request[AnyContent] =>
      println("por consecutivo")
      val empr_id = Utility.extraerEmpresa(request)
      val reporte = reporteService.buscarPorConsecutivoMovil(
        reti_id,
        repo_consecutivo,
        empr_id.get
      )
      reporte match {
        case None => {
          Future.successful(NotFound(Json.toJson("false")))
        }
        case Some(reporte) => {
          Future.successful(Ok(write(reporte)))
        }
      }
    }
    
  def consultarReporteMovil(uuid: String) = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val empr_id = Utility.extraerEmpresa(request)
      val reporte = reporteService.consultarReporteMovil(uuid)
      reporte match {
        case None => {
          Future.successful(NoContent)
        }
        case Some(rep) => {
          case class dato (
            id: Option[Int],
            consec: Option[Int]
          )
          val obj = new dato(rep._1, rep._2)
          Future.successful(Ok(Json.obj("id" -> rep._1, "consec" -> rep._2)))
        }
      }
  }

  def guardarReporteMovil()  = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      println("json: " + json)
      var reporte = net.liftweb.json.parse(json.toString).extract[ReporteDesdeMovil]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val reportenuevo = new Reporte(
        null,
        reporte.tireuc_id,
        reporte.reti_id,
        reporte.repo_consecutivo,
        Some(new DateTime()),
        reporte.repo_direccion,
        reporte.repo_nombre,
        reporte.repo_telefono,
        reporte.repo_fechasolucion,
        reporte.repo_horainicio,
        reporte.repo_horafin,
        reporte.repo_reportetecnico,
        reporte.repo_descripcion,
        reporte.repo_subrepoconsecutivo,
        reporte.rees_id,
        reporte.orig_id,
        reporte.barr_id,
        empr_id,
        reporte.tiba_id,
        usua_id,
        reporte.adicional,
        reporte.meams,
        reporte.eventos,
        reporte.direcciones,
        reporte.novedades
      )
      val uuid = reporte.uuid.getOrElse("")
      val ip_address = request.remoteAddress
      reporteService.crearFromMovil(reportenuevo, uuid, ip_address).map {
        case (id, consec) =>
          if (id > 0) {
            Created(Json.obj("id" -> id, "consec" -> consec))
          } else {
            NotAcceptable(Json.toJson("true"))
          }
      }
  }

  def actualizarReporteMovil() = authenticatedUserAction.async {
    implicit request: Request[AnyContent] =>
      val json = request.body.asJson.get
      println("Guardar Reporte Movil json: " + json)
      val fw = new FileWriter("/opt/mobile.log", true) ; 
      val currentDate = new DateTime()
      fw.write(currentDate.toString + ": Json: " + json)
      var reporte =
        net.liftweb.json.parse(json.toString).extract[Reporte]
      val usua_id = Utility.extraerUsuario(request)
      val empr_id = Utility.extraerEmpresa(request)
      val ip_address = request.remoteAddress
      val reportenuevo = new Reporte(
        reporte.repo_id,
        reporte.tireuc_id,
        reporte.reti_id,
        reporte.repo_consecutivo,
        reporte.repo_fecharecepcion,
        reporte.repo_direccion,
        reporte.repo_nombre,
        reporte.repo_telefono,
        reporte.repo_fechasolucion,
        reporte.repo_horainicio,
        reporte.repo_horafin,
        reporte.repo_reportetecnico,
        reporte.repo_descripcion,
        reporte.repo_subrepoconsecutivo,
        reporte.rees_id,
        reporte.orig_id,
        reporte.barr_id,
        empr_id,
        reporte.tiba_id,
        usua_id,
        reporte.adicional,
        reporte.meams,
        reporte.eventos,
        reporte.direcciones,
        reporte.novedades
      )
      println("reporte nuevo: " + reportenuevo)
      fw.write(currentDate.toString + ": Reporte: " + reportenuevo)
      fw.close()
      val thread = new Thread {
        override def run {
          reporteService.actualizarMovil(reportenuevo, ip_address)
        }
      }
      thread.start
      // if (reporteService.actualizarMovil(reportenuevo, ip_address)) {
      Future.successful(Ok(Json.toJson("true")))
      //} else {
      //  Future.successful(NotAcceptable(Json.toJson("true")))
      //}
  }

  def uploadFotoMovil() = authenticatedUserAction(parse.multipartFormData) { request =>
    println("Guardando Foto:" + request.toString)
      request.body
        .file("file")
        .map { f =>
          // only get the last part of the filename
          // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
          val filename = Paths.get(f.filename).getFileName // Paths.get(f.filename).getFileName
          val fileSize = f.fileSize
          val contentType = f.contentType
          val path = "/opt/siap/fotos/" //System.getProperty("java.io.tmpdir")
          println("tmp path:" + path)
          val newTempDir = new File(path)
          if (!newTempDir.exists()) {
            newTempDir.mkdirs()
          }
          val file = newTempDir + "/" + filename
          println("path file: " + file)
          f.ref.copyTo(Paths.get(s"$file"), replace = true)
/*           val txtFile = new File(filename)
          val bw = new BufferedWriter(new FileWriter(file))
          for (line <- lines) {
            bw.write(line)
          }
          bw.close() */
          Ok("Archivo Cargado")
        }
        .getOrElse {
          NotFound("Archivo No Cargado")
        }
    }

}
