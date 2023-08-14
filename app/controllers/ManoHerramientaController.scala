package controllers

import java.util.Calendar
import javax.inject.Inject
import models._
import play.api.mvc._
import play.api.libs.json._
import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import pdi.jwt.JwtSession

import utilities._


import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import dto.ResultDto
import dto.QueryDto

import org.joda.time.format.DateTimeFormat

import java.io.BufferedOutputStream
import java.io.FileOutputStream
import java.nio.file.Paths
import java.io.File

import models.ManoHerramientaRepository
import org.joda.time.DateTime

@Singleton
class ManoHerramientaController @Inject()(
    mService: ManoHerramientaRepository,
    cc: ControllerComponents,
    authenticatedUserAction: AuthenticatedUserAction)(
    implicit ec: ExecutionContext)
    extends AbstractController(cc) {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )

  def getManoObraOrden = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)
    val result = mService.getManoObraOrden(empr_id.get)
    Future.successful(Ok(write(result)))
  }

  def guardarManoObraOrden = authenticatedUserAction.async { implicit request =>
    val json = request.body.asJson.get
    var mano_obra = net.liftweb.json.parse(json.toString).extract[ManoObraOrden]
/*     val maob_id = ( json \ "maob_id").as[Long]
    val maob_descripcion = ( json \ "maob_descripcion").as[String]
    val maob_codigo = ( json \ "maob_codigo").as[Int]
 */    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)

    val maob = ManoObraOrden(mano_obra.maob_id, mano_obra.maob_descripcion, empr_id, mano_obra.maob_codigo)
    mService.guardarManoObraOrden(maob).map { result =>
      Ok(write(result))
    }
  }

  def guardarManoObraOrdenPrecio = authenticatedUserAction.async { implicit request =>
    val json = request.body.asJson.get
    val maob_id = ( json \ "maob_id").as[Long]
    val maobpr_anho = ( json \ "maobpr_anho").as[Int]
    val maobpr_fecha = ( json \ "maobpr_fecha").as[Long]
    val maobpr_precio = ( json \ "maobpr_precio").as[Double]
    val maobpr_rendimiento = ( json \ "maobpr_rendimiento").as[Double]
    val maob_codigo = maob_id.toInt
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)
    val cotr_tipo_obra = ( json \ "cotr_tipo_obra").as[Int]
    val cotr_tipo_obra_tipo = ( json \ "cotr_tipo_obra_tipo").as[String]
    val maobpr = ManoObraOrdenPrecio(Some(maob_id), Some(maobpr_anho), Some(maobpr_fecha), Some(maobpr_precio), Some(maobpr_rendimiento), Some(maob_codigo), empr_id, Some(cotr_tipo_obra), Some(cotr_tipo_obra_tipo))
    mService.guardarManoObraOrdenPrecio(maobpr).map { result =>
      Ok(write(result))
    }
  }

  def borrarManoObraOrdenPrecio = authenticatedUserAction.async { implicit request =>
    val json = request.body.asJson.get
    val maob_id = ( json \ "maob_id").as[Long]
    val maobpr_anho = ( json \ "maobpr_anho").as[Int]
    val maobpr_fecha = ( json \ "maobpr_fecha").as[Long]
    val maobpr_precio = ( json \ "maobpr_precio").as[Double]
    val maobpr_rendimiento = ( json \ "maobpr_rendimiento").as[Double]
    val maob_codigo = maob_id.toInt
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)
    val cotr_tipo_obra = ( json \ "cotr_tipo_obra").as[Int]
    val cotr_tipo_obra_tipo = ( json \ "cotr_tipo_obra_tipo").as[String]
    val maobpr = ManoObraOrdenPrecio(Some(maob_id), Some(maobpr_anho), Some(maobpr_fecha), Some(maobpr_precio), Some(maobpr_rendimiento), Some(maob_codigo), empr_id, Some(cotr_tipo_obra), Some(cotr_tipo_obra_tipo))
    mService.borrarManoObraOrdenPrecio(maobpr).map { result =>
      Ok(write(result))
    }
  }

  def getManoObraOrdenPrecio = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val json = request.body.asJson.get
    val anho = ( json \ "anho").as[Int]
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
    val usua_id = Utility.extraerUsuario(request)

    val total = mService.cuentaManoObraOrdenPrecio(empr_id.get, filtro, anho)
    mService.todosManoObraOrdenPrecio(empr_id.get, page_size, current_page, orderby, filtro, anho).map { result =>
      Ok(write(new ResultDto[(
        Option[Long],
        Option[String],
        Option[Int],
        Option[Int],
        Option[Long],
        Option[Double],
        Option[Double],
        Option[Int],
        Option[String],
        Option[Int],
        Option[Double],
      )](result, total)))
    }
  }

  def getManoObraOrdenPrecioXlsx(anho: Int) = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)
    val os = mService.todosManoObraOrdenPrecioXlsx(empr_id.get, usua_id.get, anho)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val dt = DateTime.now().getMillis()
    val filename = "Mano_Obra_Precio_Anho_" + anho.toString + "_" + fmt.print(dt) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Future.successful(
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    )    
  }

  // Mano Herramienta

  def getManoHerramientaOrden = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)
    val result = mService.getManoHerramientaOrden(empr_id.get)
    Future.successful(Ok(write(result)))
  }

  def guardarManoHerramientaOrden = authenticatedUserAction.async { implicit request =>
    val json = request.body.asJson.get
    var mano_herramienta = net.liftweb.json.parse(json.toString).extract[ManoHerramientaOrden]
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)

    val math = ManoHerramientaOrden(mano_herramienta.math_id, mano_herramienta.math_descripcion, mano_herramienta.math_codigo, empr_id)
    mService.guardarManoHerramientaOrden(math).map { result =>
      Ok(write(result))
    }
  }

  def guardarManoHerramientaOrdenPrecio = authenticatedUserAction.async { implicit request =>
    val json = request.body.asJson.get
    val math_id = ( json \ "math_id").as[Long]
    val mathpr_anho = ( json \ "mathpr_anho").as[Int]
    val mathpr_fecha = ( json \ "mathpr_fecha").as[Long]
    val mathpr_precio = ( json \ "mathpr_precio").as[Double]
    val mathpr_es_porcentaje = ( json \ "mathpr_es_porcentaje").as[Boolean]
    val mathpr_rendimiento = ( json \ "mathpr_rendimiento").as[Double]
    val mathpr_cantidad = ( json \ "mathpr_cantidad").as[Int]
    val math_codigo = math_id.toInt
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)
    val cotr_tipo_obra = ( json \ "cotr_tipo_obra").as[Int]
    val cotr_tipo_obra_tipo = ( json \ "cotr_tipo_obra_tipo").as[String]
    val mathpr = ManoHerramientaOrdenPrecio(Some(math_id), Some(mathpr_anho), Some(mathpr_fecha), Some(mathpr_precio), Some(mathpr_es_porcentaje), Some(mathpr_rendimiento), Some(mathpr_cantidad), Some(math_codigo), empr_id, Some(cotr_tipo_obra), Some(cotr_tipo_obra_tipo))
    mService.guardarManoHerramientaOrdenPrecio(mathpr).map { result =>
      Ok(write(result))
    }
  }

  def borrarManoHerramientaOrdenPrecio = authenticatedUserAction.async { implicit request =>
    val json = request.body.asJson.get
    val math_id = ( json \ "math_id").as[Long]
    val mathpr_anho = ( json \ "mathpr_anho").as[Int]
    val mathpr_fecha = ( json \ "mathpr_fecha").as[Long]
    val mathpr_precio = ( json \ "mathpr_precio").as[Double]
    val mathpr_es_porcentaje = ( json \ "mathpr_es_porcentaje").as[Boolean]
    val mathpr_rendimiento = ( json \ "mathpr_rendimiento").as[Double]
    val mathpr_cantidad = ( json \ "mathpr_cantidad").as[Int]
    val math_codigo = math_id.toInt
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)
    val cotr_tipo_obra = ( json \ "cotr_tipo_obra").as[Int]
    val cotr_tipo_obra_tipo = ( json \ "cotr_tipo_obra_tipo").as[String]
    val mathpr = ManoHerramientaOrdenPrecio(Some(math_id), Some(mathpr_anho), Some(mathpr_fecha), Some(mathpr_precio), Some(mathpr_es_porcentaje), Some(mathpr_rendimiento), Some(mathpr_cantidad), Some(math_codigo), empr_id, Some(cotr_tipo_obra), Some(cotr_tipo_obra_tipo))
    mService.borrarManoHerramientaOrdenPrecio(mathpr).map { result =>
      Ok(write(result))
    }
  }

  def getManoHerramientaOrdenPrecio = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val json = request.body.asJson.get
    val anho = ( json \ "anho").as[Int]
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
    val usua_id = Utility.extraerUsuario(request)

    val total = mService.cuentaManoHerramientaOrdenPrecio(empr_id.get, filtro, anho)
    mService.todosManoHerramientaOrdenPrecio(empr_id.get, page_size, current_page, orderby, filtro, anho).map { result =>
      Ok(write(new ResultDto[(
        Option[Long],
        Option[String],
        Option[Int],
        Option[Int],
        Option[Long],
        Option[Double],
        Option[Boolean],
        Option[Double],
        Option[Int],
        Option[Int],
        Option[String],
        Option[Int],
        Option[Double],
      )](result, total)))
    }
  }

  def getManoHerramientaOrdenPrecioXlsx(anho: Int) = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val usua_id = Utility.extraerUsuario(request)
    val os = mService.todosManoHerramientaOrdenPrecioXlsx(empr_id.get, usua_id.get, anho)
    val fmt = DateTimeFormat.forPattern("yyyyMMdd")
    val dt = DateTime.now().getMillis()
    val filename = "Mano_Transporte_Herramienta_Precio_Anho_" + anho.toString + "_" + fmt.print(dt) + ".xlsx"
    val attach = "attachment; filename=" + filename
    Future.successful(
      Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
    )    
  }


}