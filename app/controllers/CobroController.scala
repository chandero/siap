package controllers

import javax.inject.Inject
import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration
import play.api.Environment

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
import dto.ActaRedimensionamientoDto
import java.util.Calendar

import com.hhandoko.play.pdf.PdfGenerator
@Singleton
class CobroController @Inject()(
    cobro6Service: Cobro6Repository,
    cobro2Service: Cobro2Repository,
    generalService: GeneralRepository,
    config: Configuration,
    authenticatedUserAction: AuthenticatedUserAction,
    cc: ControllerComponents,
    env: Environment
)(implicit ec: ExecutionContext)
    extends AbstractController(cc)
    with ImplicitJsonFormats {
  implicit val formats = Serialization.formats(NoTypeHints) ++ List(
    DateTimeSerializer
  )
  val FONTS_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/fonts/"
  val opensans_regular = FONTS_DEFINITION_PATH + "arial.ttf"
  val pdfGen = new PdfGenerator(env, true)
  pdfGen.loadLocalFonts(Seq(opensans_regular))


  def siap_obtener_orden_trabajo_cobro_modernizacion = authenticatedUserAction.async { implicit request => 
      val empr_id = Utility.extraerEmpresa(request)
      val json = request.body.asJson.get
      // val page_size = (json \ "page_size").as[Long]
      // val current_page = (json \ "current_page").as[Long]
      val orderby = (json \ "orderby").as[String]
      val filter = (json \ "filter").as[QueryDto]
      val filtro_a = Utility.procesarFiltrado(filter)
      var filtro = filtro_a.replace("\"", "'")
      if (filtro == "()") {
        filtro = ""
      }
      cobro6Service.siap_obtener_orden_trabajo_cobro_modernizacion(empr_id.get, filtro, orderby).map { result =>
        Ok(write(result))
      }
      /*     
      reti_id match {
        case 6 => cobro6Service.siap_obtener_orden_trabajo_cobro_modernizacion(empr_id.get, 6).map { result =>
                  Ok(write(result))
                }
        case 2 => cobro2Service.siap_obtener_orden_trabajo_cobro_expansion(empr_id.get, 2).map { result =>
                  Ok(write(result))
                }
        case _ => Future.successful(Ok(write(List.empty)))
      }
      */
  }

  def siap_generar_orden_trabajo_cobro_modernizacion(
      anho: Int,
      mes: Int,
      tireuc_id: Int,
      reti_id: Long,
      cotr_consecutivo: Long
  ) = authenticatedUserAction.async { implicit request: Request[AnyContent] =>
    val empr_id = Utility.extraerEmpresa(request)
    reti_id match {
      case 6 => cobro6Service
      .siap_generar_orden_trabajo_cobro_modernizacion(
        anho,
        mes,
        tireuc_id,
        reti_id,
        empr_id.get,
        cotr_consecutivo
      )
      .map { result =>
        Ok(write(result))
      }
      case 2 => cobro2Service.siap_generar_orden_trabajo_cobro_expansion(
        anho,
        mes,
        tireuc_id,
        reti_id,
        empr_id.get,
        cotr_consecutivo
      )
      .map { result =>
        Ok(write(result))
      }
    }
  }

  def siap_orden_trabajo_cobro(cotr_id: Long, reti_id: Long) = authenticatedUserAction.async { implicit request =>
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val (cotr_consecutivo, os) = reti_id match {
        case 6 => cobro6Service.siap_orden_trabajo_cobro(empr_id.get , cotr_id, usua_id.get)
        case 2 => cobro2Service.siap_orden_trabajo_cobro(empr_id.get , cotr_id, usua_id.get)
      }
      val _prefijo_interventoria = generalService.buscarPorId(10, empr_id.get).get.gene_valor.get
      val filename = "Informe_Orden_Trabajo_" + _prefijo_interventoria + cotr_consecutivo + ".xlsx"
      val attach = "attachment; filename=" + filename
      Future.successful(Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
      )
  }

  def siap_orden_trabajo_cobro_verificar(reti_id: Long, anho: Int, mes: Int) = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val result = reti_id match {
      case 6 => cobro6Service.siap_orden_trabajo_verificar(empr_id.get, anho, mes)
      case 2 => cobro2Service.siap_orden_trabajo_verificar(empr_id.get, anho, mes)
    }
    Future.successful(Ok(write(result)))
  }

  def siap_orden_trabajo_cobro_consecutivo(reti_id: Long) = authenticatedUserAction.async { implicit request =>
    val empr_id = Utility.extraerEmpresa(request)
    val result = reti_id match {
      case 6 => cobro6Service.siap_orden_trabajo_cobro_consecutivo(empr_id.get)
      case 2 => cobro2Service.siap_orden_trabajo_cobro_consecutivo(empr_id.get)
    }
    Future.successful(Ok(write(result)))
  }

  def siap_orden_trabajo_cobro_relacion(anho:Int, periodo:Int) = authenticatedUserAction.async { implicit request =>
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val os = cobro6Service.siap_orden_trabajo_cobro_relacion(empr_id.get , anho, periodo)
      val filename = "Relacion_Orden_Trabajo_ITAF_" + anho.toString() + "_" + periodo.toString() + ".xlsx"
      val attach = "attachment; filename=" + filename
      Future.successful(Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
      )
  }

  def siap_orden_trabajo_cobro_relacion_factura() = authenticatedUserAction.async { implicit request =>
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val os = cobro6Service.siap_orden_trabajo_cobro_relacion_factura(empr_id.get)
      val filename = "Relacion_Orden_Trabajo_ITAF_Factura" + ".xlsx"
      val attach = "attachment; filename=" + filename
      Future.successful(Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
      )
  }  
  
  def siap_orden_trabajo_cobro_acta_redimensionamiento(anho:Int, periodo:Int) = authenticatedUserAction.async { implicit request =>
      val formatter = java.text.NumberFormat.getIntegerInstance
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val (_numero_acta, _valor_acumulado_anterior, _subtotal_expansion, _subtotal_modernizacion, _subtotal_desmonte, _subtotal_total) = cobro6Service.siap_orden_trabajo_cobro_acta_redimensionamiento(empr_id.get , anho, periodo, usua_id.get)
      val _periodo = Calendar.getInstance()
      _periodo.set(Calendar.YEAR, anho)
      _periodo.set(Calendar.MONTH, periodo - 1)
      _periodo.set(Calendar.DAY_OF_MONTH, 1)
      _periodo.set(Calendar.DATE, _periodo.getActualMaximum(Calendar.DATE))
      val filename = "Acta_Redimensionamiento_" + _numero_acta + "_" + anho + "_" + periodo + ".pdf"
      var _fecha_corte = _periodo.clone().asInstanceOf[Calendar]
      // _fecha_corte.add(Calendar.MONTH, -1)
      var _fecha_corte_anterior = _fecha_corte.clone().asInstanceOf[Calendar]
      _fecha_corte_anterior.add(Calendar.MONTH, -1)
      var _fecha_acta = _fecha_corte.clone().asInstanceOf[Calendar]
      _fecha_acta.add(Calendar.MONTH, 1)
      _fecha_acta.set(Calendar.DATE, 1)
      val _fecha_firma = HolidayUtil.getNextBusinessDay(_fecha_acta.getTime(), 7)
      val _total_anterior = _valor_acumulado_anterior
      val acta = new ActaRedimensionamientoDto(
        "%05d".format(_numero_acta),
        Utility.fechaamesanho(Some(new DateTime(_fecha_acta.getTime()))),
        Utility.fechaatextosindia(Some(new DateTime(_fecha_corte.getTime()))),
        Utility.fechaatextosindia(Some(new DateTime(_fecha_corte_anterior.getTime()))),
        Utility.fechaamesanho(Some(new DateTime(_fecha_corte.getTime()))),
        Utility.fechaatextofirma(Some(new DateTime(_fecha_firma))),
        "$" + formatter.format(_subtotal_total),
        "$" + formatter.format(_total_anterior + _subtotal_total),
        "$" + formatter.format(_total_anterior),
        "$" + formatter.format(_subtotal_expansion),
        "$" + formatter.format(_subtotal_modernizacion),
        "$" + formatter.format(_subtotal_desmonte),
        "$" + formatter.format(_subtotal_total),
        "$" + formatter.format(_total_anterior + _subtotal_total)
        /*, _tablaData.toList */
      )
      Future.successful(pdfGen.ok(
          views.html.siap_cobro_acta_redimensionamiento(acta),
          "conf/fonts/Arial.ttf"      
      ).withHeaders("Content-Disposition" -> s"attachment; filename=$filename"))
    }
    
  def siap_orden_trabajo_cobro_anexo_redimensionamiento(anho:Int, periodo:Int) = authenticatedUserAction.async { implicit request =>
      val formatter = java.text.NumberFormat.getIntegerInstance
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val (_numero_acta, os) = cobro6Service.siap_orden_trabajo_cobro_anexo_redimensionamiento(empr_id.get , anho, periodo, usua_id.get)
      val filename = "Anexos_Acta_Redimensionamiento_" + _numero_acta + "_" + anho + "_" + periodo + ".xlsx"
      val attach = "attachment; filename=" + filename
      Future.successful(Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
      )
    }
  }