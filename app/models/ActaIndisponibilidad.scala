package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream}
import java.util.{Map, HashMap, Date}
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream
import java.sql.Connection

// Jasper
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperRunManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JRExporter
import net.sf.jasperreports.engine.JRExporterParameter
import net.sf.jasperreports.engine.JasperCompileManager
//import net.sf.jasperreports.engine.export.JRPdfExporter
//import net.sf.jasperreports.export.SimpleExporterInput
//import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
//import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
//import net.sf.jasperreports.export.SimpleXlsxReportConfiguration
//

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, scalar, int, double, date}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.DateTimeZone
import org.joda.time.Days

// Excel Export
import com.norbitltd.spoiwo.model._
import com.norbitltd.spoiwo.model.enums.CellStyleInheritance
import com.norbitltd.spoiwo.natures.xlsx.Model2XlsxConversions._
import com.norbitltd.spoiwo.model.enums.{
  CellBorderStyle,
  CellFill,
  Pane,
  CellHorizontalAlignment => HA,
  CellVerticalAlignment => VA
}

// Utility
import utilities.Utility
import com.norbitltd.spoiwo.model.enums.PaperSize

case class ActaIndisponibilidad(
    acin_id: Option[Long],
    acin_numero: Option[Long],
    acin_anho: Option[Int],
    acin_periodo: Option[Int],
    acin_tarifa: Option[Double],
    empr_id: Option[Long],
    usua_id: Option[Long],
    acin_fechagenerado: Option[DateTime]
)

object ActaIndisponibilidad {
  val _set = {
    get[Option[Long]]("acin_id") ~
      get[Option[Long]]("acin_numero") ~
      get[Option[Int]]("acin_anho") ~
      get[Option[Int]]("acin_periodo") ~
      get[Option[Double]]("acin_tarifa") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("usua_id") ~
      get[Option[DateTime]]("acin_fechagenerado") map {
      case acin_id ~ acin_numero ~ acin_anho ~ acin_periodo ~ acin_tarifa ~ empr_id ~ usua_id ~ acin_fechagenerado =>
        ActaIndisponibilidad(
          acin_id,
          acin_numero,
          acin_anho,
          acin_periodo,
          acin_tarifa,
          empr_id,
          usua_id,
          acin_fechagenerado
        )
    }
  }
}

class ActaIndisponibilidadRepository @Inject()(
    empresaService: EmpresaRepository,
    usuarioService: UsuarioRepository,
    generalService: GeneralRepository,
    dbapi: DBApi
)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  def cuenta(fi: Long, ff: Long, empr_id: Long) = {
    var _fecha_inicial = new DateTime(fi)
    _fecha_inicial.withHourOfDay(0)
    _fecha_inicial.withMinuteOfHour(0)
    _fecha_inicial.withSecondOfMinute(0)
    _fecha_inicial.withMillisOfSecond(0)

    var _fecha_final = new DateTime(ff)
    _fecha_final.withHourOfDay(23)
    _fecha_final.withMinuteOfHour(59)
    _fecha_final.withSecondOfMinute(59)
    _fecha_final.withMillisOfSecond(999)

    db.withConnection { implicit connection =>
      val sql = SQL(
        "SELECT COUNT(*) FROM siap.acta_indisponibilidad WHERE empr_id = {empr_id} and cast(CONCAT(acin_anho,'/',acin_periodo,'/15') as DATE) between {fi} and {ff}"
      ).on(
        'empr_id -> empr_id,
        'fi -> _fecha_inicial,
        'ff -> _fecha_final
      )
      sql.as(scalar[scala.Long].single)
    }
  }

  def todos(
      fi: Long,
      ff: Long,
      empr_id: Long
  ): Future[List[ActaIndisponibilidad]] = Future[List[ActaIndisponibilidad]] {
    var _fecha_inicial = new DateTime(fi)
    _fecha_inicial.withHourOfDay(0)
    _fecha_inicial.withMinuteOfHour(0)
    _fecha_inicial.withSecondOfMinute(0)
    _fecha_inicial.withMillisOfSecond(0)

    var _fecha_final = new DateTime(ff)
    _fecha_final.withHourOfDay(23)
    _fecha_final.withMinuteOfHour(59)
    _fecha_final.withSecondOfMinute(59)
    _fecha_final.withMillisOfSecond(999)

    db.withConnection { implicit connection =>
      println("Fecha Inicial: " + new DateTime(fi))
      println("Fecha Final: " + new DateTime(ff))
      val sql = SQL(
        """SELECT * FROM siap.acta_indisponibilidad WHERE empr_id = {empr_id} AND cast(CONCAT(acin_anho,'/',acin_periodo,'/15') as DATE) BETWEEN {fi} AND {ff}
        ORDER BY acin_id ASC"""
      ).on(
        'empr_id -> empr_id,
        'fi -> _fecha_inicial,
        'ff -> _fecha_final
      )
      val result = sql.as(ActaIndisponibilidad._set *)
      result.toList
    }
  }

  def actaIndisponibilidadXls(
      acin_id: Long,
      empr_id: Long,
      usua_id: Long
  ): (Int, Array[Byte]) = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val municipio_nombre = empresa.muni_descripcion.get
    val usuario = usuarioService.buscarPorId(usua_id).get
    val _prefijo_interventoria =
      generalService.buscarPorId(10, empr_id).get.gene_valor.get
    var os = Array[Byte]()
    var _actaNumero = 0
    db.withTransaction { implicit connection =>
      // revisar si existe acta o crearla
      var actaParser = int("acin_numero") ~ int("acin_anho") ~ int(
        "acin_periodo") ~ double("acin_tarifa") ~ get[DateTime]("acin_fechagenerado") map {
        case acin_numero ~ acin_anho ~ acin_periodo ~ acin_tarifa ~ acin_fechagenerado =>
          (acin_numero, acin_anho, acin_periodo, acin_tarifa, acin_fechagenerado)
      }
      println(
        "Buscando Acta Indisponibilidad Numero de Id: " + acin_id + ", Empr_id:" + empr_id
      )
      val _actaOpt = SQL(
        "SELECT acin_numero, acin_anho, acin_periodo, acin_tarifa, acin_fechagenerado FROM siap.acta_indisponibilidad WHERE acin_id = {acin_id} AND empr_id = {empr_id}"
      ).on(
          'acin_id -> acin_id,
          'empr_id -> empr_id
        )
        .as(actaParser.single)
      val _anho = _actaOpt._2
      val _periodo = _actaOpt._3
      val _fi = new DateTime(_anho, _periodo, 1, 0, 0, 0, 0)
      val _ff = new DateTime(_anho, _periodo, 1, 0, 0, 0, 0).plusMonths(1).minusDays(1)
      val _parseMaterial = int("elem_id") ~
        str("elem_codigo") ~
        str("elem_descripcion") ~
        str("elem_unidad") ~
        double("cantidad_retirado") map {
        case a ~ b ~ c ~ d ~ e => (a, b, c, d, e)
      }
      /*       println("Buscando Material de Acta Numero de Id: " + acin_id)
      val _material = SQL(
        """SELECT
          e1.elem_id,
          e1.elem_codigo,
          e1.elem_unidad,
          e1.elem_descripcion,
          SUM(re1.even_cantidad_retirado) as cantidad_retirado
          FROM siap.reporte r1
          INNER JOIN siap.reporte_evento re1 ON re1.repo_id = r1.repo_id AND re1.even_estado < 8 AND re1.even_cantidad_retirado > 0
          INNER JOIN siap.elemento e1 ON e1.elem_id = re1.elem_id
          WHERE r1.repo_fechasolucion = {fecha_corte}
            AND r1.reti_id = {reti_id}
            AND r1.empr_id = {empr_id}
            AND r1.tireuc_id = {tireuc_id}
          GROUP BY 1,2,3,4"""
      ).on(
          'fecha_corte -> _actaOpt._2,
          'empr_id -> empr_id,
          'reti_id -> 6,
          'tireuc_id -> 1
        )
        .as(_parseMaterial *)
       */
      _actaNumero = _actaOpt._1

      val _parseFirma = str("firm_nombre") ~ str("firm_descripcion") ~ str(
        "firm_titulo"
      ) map {
        case a ~ b ~ c =>
          (a, b, c)
      }

      val _firmaGerente = SQL(
        "SELECT * FROM siap.firma WHERE firm_id = {firm_id} and empr_id = {empr_id}"
      ).on(
          'firm_id -> 8,
          'empr_id -> empr_id
        )
        .as(
          _parseFirma.single
        )

      val _firmaInterventor = SQL(
        "SELECT * FROM siap.firma WHERE firm_id = {firm_id} and empr_id = {empr_id}"
      ).on(
          'firm_id -> 9,
          'empr_id -> empr_id
        )
        .as(
          _parseFirma.single
        )

      val interventor_nombre = _firmaInterventor._1
      val interventor_descripcion = _firmaInterventor._2
      val interventor_cargo = _firmaInterventor._3

      val gerente_nombre = _firmaGerente._1
      val gerente_descripcion = _firmaGerente._2
      val gerente_cargo = _firmaGerente._3

      var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
      var _listMerged = new ListBuffer[CellRange]()
      var _listImage = new ListBuffer[Image]()

      val sheet1 = Sheet(
        name = "Acta",
        rows = {
          var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
          _listRow += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "INTERVENTORIA ALUMBRADO PÚBLICO",
                Some(0),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(4),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(5),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),              
            )
          _listRow += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "MUNICIPIO DE " + municipio_nombre,
                Some(0),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(4),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(5),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),              
            )
          _listRow += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "MANUAL DE PROCEDIMIENTOS",
                Some(0),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(4),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(5),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),              
            )

          _listRow += emptyRow()
          _listRow += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "INDISPONIBILIDAD DE LUMINARIAS DEL SALP",
                Some(0),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(4),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(5),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),              
            )          

          _listRow += emptyRow()

          _listRow += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "ACTA No. " + _prefijo_interventoria + " " + _actaNumero,
                Some(0),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(4),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(5),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),              
            )          

          _listRow += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "CORRESPONDIENTE AL MES DE " + (Utility
                .mes(_periodo))
                .toUpperCase() + " DE " + _anho,
                Some(0),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(4),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(5),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),              
            )          

          val _texto01 =
            s"En el Municipio ${municipio_nombre}, se reunieron los Ingenieros ${gerente_nombre} ${gerente_descripcion} "
          val _texto02 =
            s"de la Concesionaria ISAG S.A y ${interventor_nombre} ${interventor_descripcion}, Interventora"
          val _texto03 =
            s"del Contrato de Concesión No. ${empresa.empr_concesion.get} con el fin de calcular la Indisponibilidad de luminarias del sistema de alumbrado público del"
          val _texto04 =
            s"municipio ${municipio_nombre}, aplicando la Resolución 123 de la CREG"
          val _texto05 = s"Indisponibilidad:"
          val _texto05_1 =
            s"Es el tiempo total sobre un periodo dado, durante el cual un activo del Sistema de Alumbrado"
          val _texto06_1 =
            s"Público no está disponible para el servicio o funciona deficientemente."
          val _texto07 =
            "La formula establecida en el Artículo 24 de la Resolución CREG 123 establece que:"
          val _texto08 =
            "Las compensaciones por indisponibilidad de la infraestructura del Sistema de Alumbrado Público serán descontadas de la"
          val _texto09 =
            "remuneración del prestador de la actividad de AOM del Servicio de Alumbrado Público"
          val _texto10 =
            "El valor del consumo de energía eléctrica debido a indisponibilidad de luminarias en cada nivel de tensión n, se cálcula así:"
          val _texto11 =
            "DONDE:"
          val _texto12 =
            "VCEEIn:"
          val _texto12_1 =
            "Valor en pesos del consumo de energía eléctrica por indisponibilidad de luminarias en el nivel de tensión n"
          val _texto12A_1 =
            "Sólo se consideran aquellas luminarias que están reporteadas al SIAP como prendidas cuando deben estar apagadas."
          val _texto13 =
            s"n:"
          val _texto13_1 =
            "Nivel de tensión 1 o 2"
          val _texto14 =
            "TEEn:"
          val _texto14_1 =
            "Tarifa del suministro de energía eléctrica para el Servicio de Alumbrado Público en el nivel de tensión n en $/kWh"
          val _texto15 =
            "QIj,n:"
          val _texto15_1 =
            "Carga de la luminaria j en kW, reportada con indisponibilidad en el SIAP de Servicio de Alumbrado Público"
          val _texto16_1 =
            "en el nivel de tensión n. Incluye la carga de la bombilla de la luminaria y de los demás elementos internos"
          val _texto17_1 =
            "para su funcionamiento."
          val _texto18 =
            "TIj,n:"
          val _texto18_1 =
            "Numero total de horas de indisponibilidad de la luminaria j, reportada en el SIAP en el nivel de tensión n,"
          val _texto19_1 =
            "Son las horas desde el momento en que se reporta la anomalía, hasta cuando el prestador del Servicio de"
          val _texto20_1 =
            "Alumbrado Público la reporta en servicio normal."
          val _texto21 =
            "m:"
          val _texto21_1 =
            "Número total de luminarias del nivel de tensión n, reportadas al registro de quejas y reclamos del SIAP,"
          val _texto22_1 =
            "del municipio o distrito durante el periodo de remuneración."

          _listRow += texto1Row(_texto01)
          _listRow += texto1Row(_texto02)
          _listRow += texto1Row(_texto03)
          _listRow += texto1Row(_texto04)
          _listRow += texto2Row(_texto05, _texto05_1)
          _listRow += texto2Row("", _texto06_1)
          _listRow += texto2Row(_texto07, "")
          _listRow += texto1Row(_texto08)
          _listRow += texto1Row(_texto09)
          _listRow += emptyRow()
          _listRow += texto1Row(_texto10)
          _listRow += emptyRow()
          _listRow += emptyRow()
          _listRow += emptyRow()
          _listRow += emptyRow()
          _listRow += emptyRow()
          _listRow += emptyRow()

          _listRow += texto1Row(_texto11)
          _listRow += texto2Row(_texto12, _texto12_1)
          _listRow += texto2Row("", _texto12A_1)
          _listRow += texto2Row(_texto13, _texto13_1)
          _listRow += texto2Row(_texto14, _texto14_1)
          _listRow += texto2Row(_texto15, _texto15_1)
          _listRow += texto2Row("", _texto16_1)
          _listRow += texto2Row("", _texto17_1)
          _listRow += texto2Row(_texto18, _texto18_1)
          _listRow += texto2Row("", _texto19_1)
          _listRow += texto2Row("", _texto20_1)
          _listRow += texto2Row(_texto21, _texto21_1)
          _listRow += texto2Row("", _texto22_1)

          
          _listRow += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "PERIODO",
              Some(0),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(1),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(2),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(3),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(4),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(5),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(6),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(7),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )

          _listRow += com.norbitltd.spoiwo.model.Row(
            StringCell(
              Utility.mes(_periodo) + "-" + _anho,
              Some(0),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = false,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    // topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "QIj,n:",
              Some(1),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    // topStyle = CellBorderStyle.Thick,
                    // topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(2),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    // topStyle = CellBorderStyle.Thick,
                    // topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "TIj,n:",
              Some(3),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    // topStyle = CellBorderStyle.Thick,
                    // topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(4),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    // topStyle = CellBorderStyle.Thick,
                    // topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VCEEIn:",
              Some(5),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    // topStyle = CellBorderStyle.Thick,
                    // topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(6),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    // topStyle = CellBorderStyle.Thick,
                    // topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(7),
              style = Some(
                CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    // topStyle = CellBorderStyle.Thick,
                    // topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                    // bottomStyle = CellBorderStyle.Thin,
                    // bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listRow += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "m",
                Some(0),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = true,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thick,
                      rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "W",
                Some(1),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = true,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thick,
                      // rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "KW",
                Some(2),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = true,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thick,
                      rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = true,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black,
                      //rightStyle = CellBorderStyle.Thick,
                      //rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(4),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = true,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      //topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      //leftStyle = CellBorderStyle.Thick,
                      //leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thick,
                      rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(5),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = true,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      //topStyle = CellBorderStyle.Thick,
                      //topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black,
                      //rightStyle = CellBorderStyle.Thick,
                      //rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = true,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      //topStyle = CellBorderStyle.Thick,
                      //topColor = Color.Black,
                      //leftStyle = CellBorderStyle.Thick,
                      //leftColor = Color.Black,
                      //rightStyle = CellBorderStyle.Thick,
                      //rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = true,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thick,
                      rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
            .withHeight(new Height(18, HeightUnit.Point))
          // Aqui van los materiales
          var _idx = 41
          val _query = """select rd1.aap_id, rda1.aap_potencia, (date(r1.repo_fechasolucion) - date(r1.repo_fecharecepcion)) * 24 as horas  from siap.reporte r1
inner join siap.reporte_adicional ra1 on ra1.repo_id = r1.repo_id
inner join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id
inner join siap.reporte_direccion_dato rda1 on rda1.repo_id = rd1.repo_id and rda1.aap_id = rd1.aap_id and rd1.even_estado < 8
where r1.reti_id = 1 and ra1.acti_id in (2,40) and r1.repo_fechasolucion between {fi} and {ff} and r1.rees_id < 8
and (date(r1.repo_fechasolucion) - date(r1.repo_fecharecepcion)) > 0"""
          val _luminariasSet = SQL(_query).
            on(
              'fi -> _fi,
              'ff -> _ff
            ).as(SqlParser.int("aap_id") ~ SqlParser.double("aap_potencia") ~ SqlParser.double("horas") map (SqlParser.flatten) *)
          _luminariasSet.foreach { l =>
            _listRow += com.norbitltd.spoiwo.model.Row(
                NumericCell(
                  (_idx - 40),
                  Some(0),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("0"),
                      font = Font(
                        bold = false,
                        height = new Height(10, HeightUnit.Point),
                        fontName = "Arial"
                      ),
                      wrapText = java.lang.Boolean.FALSE,
                      horizontalAlignment = HA.Right,
                      borders = CellBorders(
                        //topStyle = CellBorderStyle.Thick,
                        //topColor = Color.Black,
                        leftStyle = CellBorderStyle.Thick,
                        leftColor = Color.Black,
                        rightStyle = CellBorderStyle.Thick,
                        rightColor = Color.Black,
                        //bottomStyle = CellBorderStyle.Thick,
                        //bottomColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  l._2,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("0"),
                      font = Font(
                        bold = false,
                        height = new Height(10, HeightUnit.Point),
                        fontName = "Arial"
                      ),
                      wrapText = java.lang.Boolean.FALSE,
                      horizontalAlignment = HA.Right,
                      borders = CellBorders(
                        //topStyle = CellBorderStyle.Thick,
                        //topColor = Color.Black,
                        leftStyle = CellBorderStyle.Thick,
                        leftColor = Color.Black,
                        rightStyle = CellBorderStyle.Thick,
                        rightColor = Color.Black,
                        //bottomStyle = CellBorderStyle.Thick,
                        //bottomColor = Color.Black
                      )                      
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "B"+(_idx+1)+"/1000",
                  Some(2),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#0.000"),
                      font = Font(
                        bold = false,
                        height = new Height(10, HeightUnit.Point),
                        fontName = "Arial"
                      ),
                      wrapText = java.lang.Boolean.FALSE,
                      horizontalAlignment = HA.Right,
                      borders = CellBorders(
                        //topStyle = CellBorderStyle.Thick,
                        //topColor = Color.Black,
                        leftStyle = CellBorderStyle.Thick,
                        leftColor = Color.Black,
                        rightStyle = CellBorderStyle.Thick,
                        rightColor = Color.Black,
                        //bottomStyle = CellBorderStyle.Thick,
                        //bottomColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  l._3,
                  Some(3),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("0"),
                      font = Font(
                        bold = false,
                        height = new Height(10, HeightUnit.Point),
                        fontName = "Arial"
                      ),
                      wrapText = java.lang.Boolean.FALSE,
                      horizontalAlignment = HA.Right,
                      borders = CellBorders(
                        //topStyle = CellBorderStyle.Thick,
                        //topColor = Color.Black,
                        leftStyle = CellBorderStyle.Thick,
                        leftColor = Color.Black,
                        //rightStyle = CellBorderStyle.Thick,
                        //rightColor = Color.Black,
                        //bottomStyle = CellBorderStyle.Thick,
                        //bottomColor = Color.Black
                      )                      
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(4),
                  style = Some(
                    CellStyle(
                      font = Font(
                        bold = false,
                        height = new Height(10, HeightUnit.Point),
                        fontName = "Arial"
                      ),
                      wrapText = java.lang.Boolean.FALSE,
                      horizontalAlignment = HA.Left,
                      borders = CellBorders(
                        //topStyle = CellBorderStyle.Thick,
                        //topColor = Color.Black,
                        // leftStyle = CellBorderStyle.Thick,
                        // leftColor = Color.Black,
                        rightStyle = CellBorderStyle.Thick,
                        rightColor = Color.Black,
                        //bottomStyle = CellBorderStyle.Thick,
                        //bottomColor = Color.Black
                      )                      
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "C"+(_idx+1)+"*D"+(_idx+1),
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("###0.000"),
                      font = Font(
                        bold = false,
                        height = new Height(10, HeightUnit.Point),
                        fontName = "Arial"
                      ),
                      wrapText = java.lang.Boolean.FALSE,
                      horizontalAlignment = HA.Center,
                      borders = CellBorders(
                        //topStyle = CellBorderStyle.Thick,
                        //topColor = Color.Black,
                        leftStyle = CellBorderStyle.Thick,
                        leftColor = Color.Black,
                        //rightStyle = CellBorderStyle.Thick,
                        //rightColor = Color.Black,
                        //bottomStyle = CellBorderStyle.Thick,
                        //bottomColor = Color.Black
                      )                      
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(6),
                  style = Some(
                    CellStyle(
                      font = Font(
                        bold = false,
                        height = new Height(10, HeightUnit.Point),
                        fontName = "Arial"
                      ),
                      wrapText = java.lang.Boolean.FALSE,
                      horizontalAlignment = HA.Left
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  "",
                  Some(7),
                  style = Some(
                    CellStyle(
                      font = Font(
                        bold = false,
                        height = new Height(10, HeightUnit.Point),
                        fontName = "Arial"
                      ),
                      wrapText = java.lang.Boolean.FALSE,
                      horizontalAlignment = HA.Left,
                      borders = CellBorders(
                        //topStyle = CellBorderStyle.Thick,
                        //topColor = Color.Black,
                        //leftStyle = CellBorderStyle.Thick,
                        //leftColor = Color.Black,
                        rightStyle = CellBorderStyle.Thick,
                        rightColor = Color.Black,
                        //bottomStyle = CellBorderStyle.Thick,
                        //bottomColor = Color.Black
                      )                      
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              .withHeight(new Height(18, HeightUnit.Point))
              _listMerged += CellRange((_idx, _idx), (3, 4))
              _listMerged += CellRange((_idx, _idx), (5, 7))
            _idx += 1
          }
          val _listaLineas = xlsSegundoBloque(
            empr_id,
            _actaOpt,
            _firmaGerente,
            _firmaInterventor,
            _idx,
            _luminariasSet.size
          )
          _listRow ++= _listaLineas._1
          _listMerged ++= _listaLineas._2

          _listRow.toList
        },
        mergedRegions = {
          _listMerged += CellRange((0, 0), (0, 7))
          _listMerged += CellRange((1, 1), (0, 7))
          _listMerged += CellRange((2, 2), (0, 7))
          _listMerged += CellRange((4, 4), (0, 7))
          _listMerged += CellRange((6, 6), (0, 7))
          _listMerged += CellRange((7, 7), (0, 7))
          _listMerged += CellRange((8, 8), (0, 7))
          _listMerged += CellRange((9, 9), (0, 7))
          _listMerged += CellRange((10, 10), (0, 7))
          _listMerged += CellRange((11, 11), (0, 7))
          _listMerged += CellRange((12, 12), (1, 7))
          _listMerged += CellRange((13, 13), (1, 7))
          _listMerged += CellRange((14, 14), (0, 7))
          _listMerged += CellRange((15, 15), (0, 7))
          _listMerged += CellRange((16, 16), (0, 7))
          _listMerged += CellRange((17, 17), (0, 7))
          _listMerged += CellRange((18, 18), (0, 7))
          _listMerged += CellRange((24, 24), (1, 7))
          _listMerged += CellRange((26, 26), (1, 7))
          _listMerged += CellRange((27, 27), (1, 7))
          _listMerged += CellRange((28, 28), (1, 7))
          _listMerged += CellRange((29, 29), (1, 7))
          _listMerged += CellRange((30, 30), (1, 7))
          _listMerged += CellRange((31, 31), (1, 7))
          _listMerged += CellRange((32, 32), (1, 7))
          _listMerged += CellRange((33, 33), (1, 7))
          _listMerged += CellRange((34, 34), (1, 7))
          _listMerged += CellRange((35, 35), (1, 7))
          _listMerged += CellRange((36, 36), (1, 7))
          _listMerged += CellRange((37, 37), (1, 7))
          _listMerged += CellRange((39, 39), (1, 2))
          _listMerged += CellRange((39, 39), (3, 4))
          _listMerged += CellRange((39, 39), (5, 7))

          _listMerged.toList
        },
        columns = {
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 0, width = new Width(15, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 1, width = new Width(12, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 2, width = new Width(12, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 3, width = new Width(12, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 4, width = new Width(12, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 5, width = new Width(12, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 6, width = new Width(12, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 7, width = new Width(18, WidthUnit.Character))
          _listColumn.toList
        },
        images = {
          _listImage += com.norbitltd.spoiwo.model.Image(
            region = CellRange((20, 25),(2,6)),
            filePath = s"${System.getProperty("user.dir")}/public/img/formula_indisponibilidad.png"
          )
          _listImage.toList
        }
      )
      var sos: ByteArrayOutputStream = new ByteArrayOutputStream()
/*       val printSetup = PrintSetup(
        paperSize = PaperSize.Letter,
        pageStart = 1,
        fitToPage = true,
      ) */
      var printSetup = PrintSetup()
      printSetup.withPaperSize(PaperSize.Letter)
      printSetup.withPageStart(1)
      printSetup.withScale(82)
      sheet1.withPrintSetup(printSetup)
      Workbook(sheet1).writeToOutputStream(sos)
      os = sos.toByteArray
    }
    (_actaNumero, os)
  }

  def xlsSegundoBloque(
      empr_id: Long,
      _actaOpt: (Int, Int, Int, Double, DateTime),
      _firmaGerente: (String, String, String),
      _firmaInterventor: (String, String, String),
      idx: Int,
      _operaciones: Int = 0
  )(implicit connection: Connection) = {
    // Busco firmantes
    println("Buscando Firmas..")
    val fecha_firma = Utility.fechaatextosindia(Some(_actaOpt._5))

    val interventor_nombre = _firmaInterventor._1
    val interventor_descripcion = _firmaInterventor._2
    val interventor_cargo = _firmaInterventor._3

    val gerente_nombre = _firmaGerente._1
    val gerente_descripcion = _firmaGerente._2
    val gerente_cargo = _firmaGerente._3
    var _listRow = ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged = new ListBuffer[CellRange]()
    val _texto23 =
      "Observaciones:"
    val _texto24 = _operaciones match {
      case 0 => "No se reportaron al SIAP luminarias como prendidas cuando deben estar apagadas y se evidenció que"
      case _ => s"Se reportaron al SIAP ${_operaciones} luminarias como prendidas cuando deben estar apagadas y se evidenció que"
    }
    val _texto25 =
      "la ESSA no cobró energía por concepto de luminarias prendidas de día."
    val _texto26 = "En constancia firman los interesados el día " + fecha_firma

    var _idx = idx
    _listRow += com.norbitltd.spoiwo.model.Row(
      StringCell(
        "SUMATORIA DE QIJ,n: * TIJ,n:",
        Some(0),
        style = Some(
          CellStyle(
            font = Font(
              bold = true,
              height = new Height(10, HeightUnit.Point),
              fontName = "Arial"
            ),
            horizontalAlignment = HA.Left,
            borders = CellBorders(
              topStyle = CellBorderStyle.Thick,
              topColor = Color.Black,
              leftStyle = CellBorderStyle.Thick,
              leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(1),
        style = Some(
          CellStyle(
            font = Font(
              bold = true,
              height = new Height(10, HeightUnit.Point),
              fontName = "Arial"
            ),
            horizontalAlignment = HA.Center,
            borders = CellBorders(
              topStyle = CellBorderStyle.Thick,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(2),
        style = Some(
          CellStyle(
            font = Font(
              bold = true,
              height = new Height(10, HeightUnit.Point),
              fontName = "Arial"
            ),
            horizontalAlignment = HA.Center,
            borders = CellBorders(
              topStyle = CellBorderStyle.Thick,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(3),
        style = Some(
          CellStyle(
            font = Font(
              bold = true,
              height = new Height(10, HeightUnit.Point),
              fontName = "Arial"
            ),
            horizontalAlignment = HA.Center,
            borders = CellBorders(
              topStyle = CellBorderStyle.Thick,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(4),
        style = Some(
          CellStyle(
            font = Font(
              bold = true,
              height = new Height(10, HeightUnit.Point),
              fontName = "Arial"
            ),
            horizontalAlignment = HA.Center,
            borders = CellBorders(
              topStyle = CellBorderStyle.Thick,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              rightStyle = CellBorderStyle.Thick,
              rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      FormulaCell(
        "SUM(F42:F"+_idx+")",
        Some(5),
        style = Some(
          CellStyle(
            font = Font(
              bold = true,
              height = new Height(10, HeightUnit.Point),
              fontName = "Arial"
            ),
            horizontalAlignment = HA.Center,
            borders = CellBorders(
              topStyle = CellBorderStyle.Thick,
              topColor = Color.Black,
              leftStyle = CellBorderStyle.Thick,
              leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(6),
        style = Some(
          CellStyle(
            font = Font(
              bold = true,
              height = new Height(10, HeightUnit.Point),
              fontName = "Arial"
            ),
            horizontalAlignment = HA.Center,
            borders = CellBorders(
              topStyle = CellBorderStyle.Thick,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(7),
        style = Some(
          CellStyle(
            font = Font(
              bold = true,
              height = new Height(10, HeightUnit.Point),
              fontName = "Arial"
            ),
            horizontalAlignment = HA.Center,
            borders = CellBorders(
              topStyle = CellBorderStyle.Thick,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              rightStyle = CellBorderStyle.Thick,
              rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      )
    )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model
      .Row(
        StringCell(
          "",
          Some(0),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(2),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(3),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(4),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(5),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(6),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(7),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model
      .Row(
        StringCell(
          "TEEn:",
          Some(0),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                //leftStyle = CellBorderStyle.Thick,
                //leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(2),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                //leftStyle = CellBorderStyle.Thick,
                //leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        NumericCell(
          _actaOpt._4,
          Some(3),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("$#,##0.00"),
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Right,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(4),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                //leftStyle = CellBorderStyle.Thick,
                //leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(5),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(6),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                //leftStyle = CellBorderStyle.Thick,
                //leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(7),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                //topStyle = CellBorderStyle.Thick,
                //topColor = Color.Black,
                //leftStyle = CellBorderStyle.Thick,
                //leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      .withHeight(new Height(18, HeightUnit.Point))
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model
      .Row(
        StringCell(
          "",
          Some(0),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(2),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(3),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(4),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(5),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(6),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(7),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model
      .Row(
        StringCell(
          "VCEEIn:",
          Some(0),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(2),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(3),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(4),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        FormulaCell(
          "ROUNDUP(D" + (_idx - 1) + "*F" + (_idx - 3) + ",0)",
          Some(5),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("$#,##0.00"),
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(6),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(7),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model
      .Row(
        StringCell(
          _texto23,
          Some(0),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Left,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(2),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(3),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(4),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(5),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(6),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(7),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model
      .Row(
        StringCell(
          _texto24,
          Some(0),
          style = Some(
            CellStyle(
              font = Font(
                bold = false,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Left,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(2),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(3),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(4),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(5),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                //topStyle = CellBorderStyle.Thick,
                //topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(6),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                //topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(7),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                //topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black
                //bottomStyle = CellBorderStyle.Thick,
                //bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model
      .Row(
        StringCell(
          _texto25,
          Some(0),
          style = Some(
            CellStyle(
              font = Font(
                bold = false,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Left,
              borders = CellBorders(
                //topStyle = CellBorderStyle.Thick,
                //topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                //topStyle = CellBorderStyle.Thick,
                //topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(2),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                //topStyle = CellBorderStyle.Thick,
                //topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(3),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(4),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(5),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(6),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                // rightStyle = CellBorderStyle.Thick,
                // rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(7),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                // topStyle = CellBorderStyle.Thick,
                // topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model
      .Row(
        StringCell(
          _texto26,
          Some(0),
          style = Some(
            CellStyle(
              font = Font(
                bold = false,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Left,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(2),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(3),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                 topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(4),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(5),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(6),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                //rightStyle = CellBorderStyle.Thick,
                //rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(7),
          style = Some(
            CellStyle(
              font = Font(
                bold = true,
                height = new Height(10, HeightUnit.Point),
                fontName = "Arial"
              ),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                // leftStyle = CellBorderStyle.Thick,
                // leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                // bottomStyle = CellBorderStyle.Thick,
                // bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )      
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model.Row(
      StringCell(
        "",
        Some(0),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              //topStyle = CellBorderStyle.Thick,
              //topColor = Color.Black,
              leftStyle = CellBorderStyle.Thick,
              leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(1),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              //topStyle = CellBorderStyle.Thick,
              //topColor = Color.Black
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(2),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(3),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(4),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(5),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(6),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(7),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              rightStyle = CellBorderStyle.Thick,
              rightColor = Color.Black
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      )
    )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model.Row(
      StringCell(
        "",
        Some(0),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              leftStyle = CellBorderStyle.Thick,
              leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(1),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(2),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(3),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(4),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(5),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(6),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(7),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              rightStyle = CellBorderStyle.Thick,
              rightColor = Color.Black
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      )
    )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model.Row(
      StringCell(
        "",
        Some(0),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              leftStyle = CellBorderStyle.Thick,
              leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(1),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(2),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(3),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(4),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(5),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(6),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(7),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              rightStyle = CellBorderStyle.Thick,
              rightColor = Color.Black
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      )
    )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model.Row(
      StringCell(
        "",
        Some(0),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              leftStyle = CellBorderStyle.Thick,
              leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(1),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(2),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(3),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(4),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(5),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(6),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thin,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(7),
        style = Some(
          CellStyle(
            wrapText = java.lang.Boolean.TRUE,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              rightStyle = CellBorderStyle.Thick,
              rightColor = Color.Black
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      )
    )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model.Row(
      StringCell(
        interventor_nombre,
        Some(0),
        style = Some(
          CellStyle(
            dataFormat = CellDataFormat("@"),
            horizontalAlignment = HA.Left,
            font = Font(
              bold = java.lang.Boolean.TRUE
            ),
            borders = CellBorders(
              topStyle = CellBorderStyle.Thin,
              topColor = Color.Black,
              leftStyle = CellBorderStyle.Thick,
              leftColor = Color.Black
              //rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(1),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
        borders = CellBorders(
              topStyle = CellBorderStyle.Thin,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              //rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            ))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(2),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
        borders = CellBorders(
              topStyle = CellBorderStyle.Thin,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              //rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            ))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),      
      StringCell(
        "",
        Some(3),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(4),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        gerente_nombre,
        Some(5),
        style = Some(
          CellStyle(
            dataFormat = CellDataFormat("@"),
            horizontalAlignment = HA.Left,
            font = Font(
              bold = java.lang.Boolean.TRUE
            ),
            borders = CellBorders(
              topStyle = CellBorderStyle.Thin,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              //rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(6),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
        borders = CellBorders(
              topStyle = CellBorderStyle.Thin,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              //rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            ))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(7),
        style = Some(
          CellStyle(
            dataFormat = CellDataFormat("@"),
            borders = CellBorders(
              topStyle = CellBorderStyle.Thin,
              topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              rightStyle = CellBorderStyle.Thick,
              rightColor = Color.Black
              // bottomStyle = CellBorderStyle.Thin,
              // bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      )
    )
    _idx += 1
    _listRow += com.norbitltd.spoiwo.model.Row(
      StringCell(
        interventor_cargo,
        Some(0),
        style = Some(
          CellStyle(
            dataFormat = CellDataFormat("@"),
            horizontalAlignment = HA.Left,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              leftStyle = CellBorderStyle.Thick,
              leftColor = Color.Black,
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(1),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),            
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            ))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(2),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),            
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            ))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),      
      StringCell(
        "",
        Some(3),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
        borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            ))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(4),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
        borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            ))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        gerente_cargo,
        Some(5),
        style = Some(
          CellStyle(
            dataFormat = CellDataFormat("@"),
            horizontalAlignment = HA.Left,
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )            
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(6),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
        borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black
              // rightStyle = CellBorderStyle.Thick,
              // rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            ))),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "",
        Some(7),
        style = Some(
          CellStyle(
            dataFormat = CellDataFormat("@"),
            borders = CellBorders(
              // topStyle = CellBorderStyle.Thick,
              // topColor = Color.Black,
              // leftStyle = CellBorderStyle.Thick,
              // leftColor = Color.Black,
              rightStyle = CellBorderStyle.Thick,
              rightColor = Color.Black,
              bottomStyle = CellBorderStyle.Thick,
              bottomColor = Color.Black
            )
          )
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      )
    )

    _listMerged += CellRange((_idx - 12, _idx - 12), (3, 4))
    _listMerged += CellRange((_idx - 14, _idx - 14), (5, 7))
    _listMerged += CellRange((_idx - 10, _idx - 10), (5, 7))

    (_listRow, _listMerged)
  }

  def actaIndisponibilidadGenerador(
      anho: Int,
      periodo: Int,
      tarifa: Double,
      empr_id: Long,
      usua_id: Long
  ): Boolean = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val municipio_nombre = empresa.muni_descripcion.get
    val usuario = usuarioService.buscarPorId(usua_id).get
    println("Creando Acta")
    var _actaNumero = 0
    db.withTransaction { implicit connection =>
      // revisar si existe acta o crearla
      val _acta = SQL(
        """SELECT acin_numero FROM siap.acta_indisponibilidad WHERE acin_anho = {anho} and acin_periodo = {periodo} AND empr_id = {empr_id}"""
      ).on(
          'anho -> anho,
          'periodo -> periodo,
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[Long].singleOpt)
      _acta match {
        case None =>
          var _siguiente = SQL(
            "SELECT gene_numero FROM siap.general WHERE gene_id = {gene_id}"
          ).on(
              'gene_id -> 11
            )
            .as(SqlParser.scalar[Int].single)
          _siguiente += 1
          SQL(
            "UPDATE siap.general SET gene_numero = {gene_numero} WHERE gene_id = {gene_id}"
          ).on(
              'gene_numero -> _siguiente,
              'gene_id -> 11
            )
            .executeUpdate()
          SQL(
            "INSERT INTO siap.acta_indisponibilidad (acin_numero, acin_anho, acin_periodo, acin_tarifa, acin_fechagenerado, empr_id, usua_id) VALUES ({acin_numero}, {acin_anho}, {acin_periodo}, {acin_tarifa}, {acin_fechagenerado}, {empr_id}, {usua_id})"
          ).on(
              'acin_numero -> _siguiente,
              'acin_anho -> anho,
              'acin_periodo -> periodo,
              'acin_tarifa -> tarifa,
              'acin_fechagenerado -> new DateTime(),
              'empr_id -> empr_id,
              'usua_id -> usua_id
            )
            .executeUpdate()

        case _ => println("Ya Existe el Acta")
      }
      true
    }
  }

  def emptyRow(): com.norbitltd.spoiwo.model.Row = {
    com.norbitltd.spoiwo.model.Row(
      StringCell(
        "", 
        Some(0),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
          borders = CellBorders(
            //topStyle = CellBorderStyle.Thick,
            //topColor = Color.Black,
            leftStyle = CellBorderStyle.Thick,
            leftColor = Color.Black,
            //rightStyle = CellBorderStyle.Thick,
            //rightColor = Color.Black,
            //bottomStyle = CellBorderStyle.Thick,
            //bottomColor = Color.Black
          ))
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "", 
        Some(1),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
          borders = CellBorders(
            //topStyle = CellBorderStyle.Thick,
            //topColor = Color.Black,
            // leftStyle = CellBorderStyle.Thick,
            // leftColor = Color.Black,
            //rightStyle = CellBorderStyle.Thick,
            //rightColor = Color.Black,
            //bottomStyle = CellBorderStyle.Thick,
            //bottomColor = Color.Black
          ))
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "", 
        Some(2),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
          borders = CellBorders(
            //topStyle = CellBorderStyle.Thick,
            //topColor = Color.Black,
            //leftStyle = CellBorderStyle.Thick,
            //leftColor = Color.Black,
            //rightStyle = CellBorderStyle.Thick,
            //rightColor = Color.Black,
            //bottomStyle = CellBorderStyle.Thick,
            //bottomColor = Color.Black
          ))
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "", 
        Some(3),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
          borders = CellBorders(
            //topStyle = CellBorderStyle.Thick,
            //topColor = Color.Black,
            //leftStyle = CellBorderStyle.Thick,
            //leftColor = Color.Black,
            //rightStyle = CellBorderStyle.Thick,
            //rightColor = Color.Black,
            //bottomStyle = CellBorderStyle.Thick,
            //bottomColor = Color.Black
          ))
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ), 
      StringCell(
        "", 
        Some(4),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
          borders = CellBorders(
            //topStyle = CellBorderStyle.Thick,
            //topColor = Color.Black,
            //leftStyle = CellBorderStyle.Thick,
            //leftColor = Color.Black,
            //rightStyle = CellBorderStyle.Thick,
            //rightColor = Color.Black,
            //bottomStyle = CellBorderStyle.Thick,
            //bottomColor = Color.Black
          ))
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),  
      StringCell(
        "", 
        Some(5),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
          borders = CellBorders(
            //topStyle = CellBorderStyle.Thick,
            //topColor = Color.Black,
            //leftStyle = CellBorderStyle.Thick,
            //leftColor = Color.Black,
            //rightStyle = CellBorderStyle.Thick,
            //rightColor = Color.Black,
            //bottomStyle = CellBorderStyle.Thick,
            //bottomColor = Color.Black
          ))
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "", 
        Some(6),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
          borders = CellBorders(
            //topStyle = CellBorderStyle.Thick,
            //topColor = Color.Black,
            //leftStyle = CellBorderStyle.Thick,
            //leftColor = Color.Black,
            //rightStyle = CellBorderStyle.Thick,
            //rightColor = Color.Black,
            //bottomStyle = CellBorderStyle.Thick,
            //bottomColor = Color.Black
          ))
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),
      StringCell(
        "", 
        Some(7),
        style = Some(CellStyle(dataFormat = CellDataFormat("@"),
          borders = CellBorders(
            //topStyle = CellBorderStyle.Thick,
            //topColor = Color.Black,
            //leftStyle = CellBorderStyle.Thick,
            //leftColor = Color.Black,
            rightStyle = CellBorderStyle.Thick,
            rightColor = Color.Black,
            //bottomStyle = CellBorderStyle.Thick,
            //bottomColor = Color.Black
          ))
        ),
        CellStyleInheritance.CellThenRowThenColumnThenSheet
      ),                
    )     
  }

  def texto1Row(_texto: String): com.norbitltd.spoiwo.model.Row = {
          com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                _texto,
                Some(0),
                style = Some(CellStyle(
                  font = Font(
                    bold = false,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Left,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(4),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(5),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),              
            )
  }

  def texto2Row(_texto: String, _texto1: String): com.norbitltd.spoiwo.model.Row = {
          com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                _texto,
                Some(0),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Left,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                _texto1,
                Some(1),
                style = Some(CellStyle(
                  font = Font(
                    bold = false,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Left,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(4),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(5),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    // rightStyle = CellBorderStyle.Thick,
                    // rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    //topStyle = CellBorderStyle.Thick,
                    //topColor = Color.Black,
                    // leftStyle = CellBorderStyle.Thick,
                    // leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black,
                    //bottomStyle = CellBorderStyle.Thick,
                    //bottomColor = Color.Black
                  )
                )),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),              
            )
  }

}
