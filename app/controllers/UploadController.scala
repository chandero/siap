package controllers

import javax.inject.Inject
import java.time.Instant
import java.util.UUID.randomUUID
import java.nio.file.Paths
import java.io.File
import java.io.FileReader

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
import scala.io.Source

import utilities._
import java.io.FileReader

import org.apache.commons.io.FileUtils
import java.util.Base64

@Singleton
class UploadController @Inject()(
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction,
    config: Configuration)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {

// Cartera
def upload_medidor_lectura(anho: Int, mes: Int, empr_id: Long) = Action(parse.multipartFormData) { request =>
  request.body
    .file("file_medidor_lectura")
    .map { f =>
      // only get the last part of the filename
      // otherwise someone can send a path like ../../home/foo/bar.txt to write to other files on the system
      val filename    = "file_medidor_lectura_" + anho + "_" + mes + "_" + empr_id + ".xlsx" // Paths.get(f.filename).getFileName
      val fileSize    = f.fileSize
      val contentType = f.contentType
      val path = "/opt/tmp/" // System.getProperty("java.io.tmpdir")
      println("tmp path:" + path)
      val newTempDir = new File(path, "cargasiap")
      if (!newTempDir.exists()) {
          newTempDir.mkdirs()
      }
      val file = newTempDir + "/" +filename
      println("tmp file: " + file)      
      f.ref.copyTo(Paths.get(s"$file"), replace = true)
      Ok("Archivo Cargado")
    }
    .getOrElse {
      NotFound("Archivo No Cargado")
    }
  }   

}