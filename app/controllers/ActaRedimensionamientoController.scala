package controllers

import javax.inject.Inject
import models._

import play.api.mvc._
import play.api.libs.json._
import play.api.Configuration
import play.api.Environment
import play.filters.csrf._
import play.filters.csrf.CSRF.Token

import com.google.inject.Singleton
import scala.concurrent.{ExecutionContext, Future}
import scala.util.{Success, Failure}

import net.liftweb.json._
import net.liftweb.json.Serialization.write
import net.liftweb.json.Serialization.read
import net.liftweb.json.parse

import org.joda.time.DateTime

import pdi.jwt.JwtSession

import models.ActaRedimensionamientoRepository

import java.util.Calendar
import java.util.HashMap
import com.hhandoko.play.pdf.PdfGenerator

import utilities._

import dto.QueryDto
import dto.ResultDto
import dto.ActaRedimensionamientoDto

@Singleton
class ActaRedimensionamientoController @Inject()(
    aService: ActaRedimensionamientoRepository,
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

    def getActasRedimensionamiento = authenticatedUserAction.async {
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
                val total = aService.cuenta(empr_id.get, filtro)
                aService
                    .getActasRedimensionamiento(empr_id.get, page_size, current_page, orderby, filtro)
                    .map { actas =>
                        Ok(write(new ResultDto[ActaRedimensionamiento](actas, total)))
                    }
        }

    def getActaRedimensionamiento(anho:Int, periodo:Int) = authenticatedUserAction.async { implicit request =>
      val formatter = java.text.NumberFormat.getIntegerInstance
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val (_numero_acta, _valor_acumulado_anterior, _subtotal_expansion, _subtotal_modernizacion, _subtotal_desmonte, _subtotal_total) = aService.getActaRedimensionamiento(empr_id.get , anho, periodo, usua_id.get)
      val _periodo = Calendar.getInstance()
      _periodo.set(Calendar.YEAR, anho)
      _periodo.set(Calendar.MONTH, periodo - 1)
      _periodo.set(Calendar.DAY_OF_MONTH, 1)
      _periodo.set(Calendar.DATE, _periodo.getActualMaximum(Calendar.DATE))
      val filename = "Acta_Redimensionamiento_" + _numero_acta + "_" + anho + "_" + periodo + ".docx"
      var _fecha_corte = _periodo.clone().asInstanceOf[Calendar]
      // _fecha_corte.add(Calendar.MONTH, -1)
      var _fecha_corte_anterior = _fecha_corte.clone().asInstanceOf[Calendar]
      _fecha_corte_anterior.add(Calendar.MONTH, -1)
      val _lastday = _fecha_corte_anterior.getActualMaximum(Calendar.DAY_OF_MONTH)
      _fecha_corte_anterior.set(Calendar.DAY_OF_MONTH, _lastday)
      var _fecha_acta = _fecha_corte.clone().asInstanceOf[Calendar]
      _fecha_acta.add(Calendar.MONTH, 1)
      _fecha_acta.set(Calendar.DATE, 1)
      val _fecha_firma = HolidayUtil.getNextBusinessDay(_fecha_acta.getTime(), 7)
      val _total_anterior = _valor_acumulado_anterior
      val acta = new ActaRedimensionamientoDto(
        "%05d".format(_numero_acta),
        Utility.fechaamesanho(Some(new DateTime(_fecha_corte.getTime()))),
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

        var template_data: HashMap[String, Object] =
          new HashMap[String, Object]()
        template_data.put("ACTA_NUMERO", acta.numero)
        template_data.put("PERIODO", acta.periodo)
        template_data.put("PERIODO_CON_DE", acta.periodo_corte)
        template_data.put("PERIODO_ANTERIOR_LETRAS", acta.fecha_corte_anterior)
        template_data.put("PERIODO_ANTERIOR_CON_DE", acta.periodo_corte)
        template_data.put("VALOR_ACUMULADO_ANTERIOR", acta.valor_acumulado_anterior)
        template_data.put("PERIODO_LETRAS", acta.fecha_corte)
        template_data.put("VALOR_ACUMULADO", acta.valor_acumulado)
        template_data.put("FECHA_FIRMA", acta.fecha_firma)

        val os = DocxGenerator.generateDocxFileFromTemplate2(
          "siap_0101_template_acta_redimensionamiento.dotx",
          template_data
        )
        val attach = "attachment; filename=" + filename
        Future.successful(
          Ok(os)
            .as(
              "application/vnd.openxmlformats-officedocument.wordprocessingml.document"
            )
            .withHeaders("Content-Disposition" -> attach)
        )
    }
    
    def getAnexoRedimensionamiento(anho:Int, periodo:Int) = authenticatedUserAction.async { implicit request =>
      val formatter = java.text.NumberFormat.getIntegerInstance
      val empr_id = Utility.extraerEmpresa(request)
      val usua_id = Utility.extraerUsuario(request)
      val (_numero_acta, os) = aService.getAnexoRedimensionamiento(empr_id.get , anho, periodo, usua_id.get)
      val filename = "Anexos_Acta_Redimensionamiento_" + _numero_acta + "_" + anho + "_" + periodo + ".xlsx"
      val attach = "attachment; filename=" + filename
      Future.successful(Ok(os)
        .as("application/vnd.openxmlformats-officedocument.spreadsheetml.sheet")
        .withHeaders("Content-Disposition" -> attach)
      )
    }
}