package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream}
import java.util.{Map, HashMap, Date}
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream

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

case class ActaIndisponibilidad(
  acin_id: Option[Long],
  acin_numero: Option[Long],
  acin_fecha: Option[DateTime],
  empr_id: Option[Long],
  usua_id: Option[Long],
  acin_fechagenerado: Option[DateTime]
)

object ActaIndisponibilidad {
  val _set = {
    get[Option[Long]]("acin_id") ~
    get[Option[Long]]("acin_numero") ~
    get[Option[DateTime]]("acin_fecha") ~
    get[Option[Long]]("empr_id") ~
    get[Option[Long]]("usua_id") ~
    get[Option[DateTime]]("acin_fechagenerado") map {
      case acin_id ~ acin_numero ~ acin_fecha ~ empr_id ~ usua_id ~ acin_fechagenerado =>
        ActaIndisponibilidad(acin_id, acin_numero, acin_fecha, empr_id, usua_id, acin_fechagenerado)
    }
  }
}

class ActaIndisponibilidadRepository @Inject()(empresaService: EmpresaRepository, usuarioService: UsuarioRepository, dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  def cuenta(fi:Long, ff:Long, empr_id: Long) = {
    db.withConnection { implicit connection =>
      val sql = SQL("SELECT COUNT(*) FROM siap.acta_indisponibilidad WHERE empr_id = {empr_id} and acin_fecha between {fi} and {ff}")
        .on(
          'empr_id -> empr_id,
          'fi -> new DateTime(fi),
          'ff -> new DateTime(ff)
        )
      sql.as(scalar[scala.Long].single)
    }
  }

  def todos(fi:Long, ff: Long, empr_id: Long):Future[List[ActaIndisponibilidad]] = Future[List[ActaIndisponibilidad]] {
    db.withConnection { implicit connection =>
      val sql = SQL("""SELECT * FROM siap.acta_indisponibilidad WHERE empr_id = {empr_id} AND acin_fecha BETWEEN {fi} AND {ff}
        ORDER BY acin_fecha ASC""")
        .on(
          'empr_id -> empr_id,
          'fi -> new DateTime(fi),
          'ff -> new DateTime(ff)
        )
      val result = sql.as(ActaIndisponibilidad._set *)
      result.toList
    }
  }

  def ActaIndisponibilidadXls(
      acin_id: Long,
      empr_id: Long,
      usua_id: Long
  ): (Int, Array[Byte]) = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val municipio_nombre = empresa.muni_descripcion.get
    val usuario = usuarioService.buscarPorId(usua_id).get
    var os = Array[Byte]()
    var _actaNumero = 0
    db.withTransaction { implicit connection =>
      // revisar si existe acta o crearla
      var actaParser = int("acin_numero") ~ get[DateTime]("acin_fecha") map {
        case acin_numero ~ acin_fecha =>
          (acin_numero, acin_fecha)
      }
      println("Buscando Acta Indisponibilidad Numero de Id: " + acin_id + ", Empr_id:" + empr_id)
      val _actaOpt = SQL(
        "SELECT acin_numero, acin_fecha FROM siap.acta_indisponibilidad WHERE acin_id = {acin_id} AND empr_id = {empr_id}"
      ).on(
          'acin_id -> acin_id,
          'empr_id -> empr_id
        )
        .as(actaParser.single)
      val _parseMaterial = int("elem_id") ~
        str("elem_codigo") ~
        str("elem_descripcion") ~
        str("elem_unidad") ~
        double("cantidad_retirado") map {
        case a ~ b ~ c ~ d ~ e => (a, b, c, d, e)
      }
      println("Buscando Material de Acta Numero de Id: " + acin_id)
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
      _actaNumero = _actaOpt._1
        // Busco firmantes
      println("Buscando Firmas..")
      val fecha_firma = Utility.fechaatextosindia(Some(_actaOpt._2))
      val _parseFirma = str("firm_nombre") ~ str("firm_titulo") map {
          case a ~ b =>
            (a, b)
      }
      val _firmaGerente = SQL(
         "SELECT * FROM siap.firma WHERE firm_id = {firm_id} and empr_id = {empr_id}"
      ).on(
            'firm_id -> 1,
            'empr_id -> empr_id
        )
        .as(
            _parseFirma.single
        )

      val _firmaInterventor = SQL(
          "SELECT * FROM siap.firma WHERE firm_id = {firm_id} and empr_id = {empr_id}"
        ).on(
            'firm_id -> 2,
            'empr_id -> empr_id
          )
          .as(
            _parseFirma.single
          )

      val _firmaAlmacen = SQL(
          "SELECT * FROM siap.firma WHERE firm_id = {firm_id} and empr_id = {empr_id}"
        ).on(
            'firm_id -> 5,
            'empr_id -> empr_id
          )
          .as(
            _parseFirma.single
          )

      val interventor_nombre = _firmaInterventor._1
      val interventor_cargo = _firmaInterventor._2

      val gerente_nombre = _firmaGerente._1
      val gerente_cargo = _firmaGerente._2

      val almacen_nombre = _firmaAlmacen._1
      val almacen_cargo = _firmaAlmacen._2

      var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
      var _listMerged = new ListBuffer[CellRange]()

      val sheet1 = Sheet(
          name = "Acta",
          rows = {
            var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center
                )
              )
              .withCellValues("INTERVENTORIA ALUMBRADO PÚBLICO")
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center
                )
              )
              .withCellValues(municipio_nombre)
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Center
                )
              )
              .withCellValues("MANUAL DE PROCEDIMIENTOS")
            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues("", "", "", "", "", "", "", "", "")
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(12, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Left
                )
              )
              .withCellValues("INDISPONIBILIDAD DE LUMINARIAS DEL SALP")
            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues("", "", "", "", "", "", "", "", "")
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(12, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Left
                )
              )
              .withCellValues("ACTA No." + _prefijo_interventoria + " " + _actaNumero)
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font = Font(
                    bold = true,
                    height = new Height(12, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Left
                )
              )
              .withCellValues("CORRESPONDIENTE AL MES DE " + Utility.mes(periodo) + "DE" + anho)
            val _texto01 =
              s"En el Municipio ${municipio_nombre}, se reunieron los ${gerente_nombre} ${gerente_cargo} y ${interventor_nombre} ${interventor_cargo}, Interventora"
            val _texto02 =
              s"del Contrato de Concesión No. ${contrato_numero} con el fin de calcular la Indisponibilidad de luminarias el sistema de alumbrado público del"
            val _texto03 =
              s"municipio de ${municipio_nombre}, aplicando la Resolución 123 de la CREG"
            val _texto04 = s"Es el tiempo total sobre un periodo dado, durante el cual un activo del Sistema de Alumbrado"
            val _texto05 =
              s"Público no está disponible para el servicio o funciona deficientemente."
            val _texto06 =
              "La formula establecida en el Artículo 24 de la Resolución CREG 123 establece que:"
            val _texto07 =
              "Las compensaciones por indisponibilidad de la infraestructura del Sistema de Alumbrado Público serán descontadas de la"
            val _texto08 =
              "remuneración del prestador de la actividad de AOM del Servicio de Alumbrado Público"
            val _texto09 =
              "El valor del consumo de energía eléctrica debido a indisponibilidad de luminarias en cada nivel de tensión n, se cálcula así:"
            val _texto10 =
              "DONDE:"
            val _texto11 =
              "VCEEIn:"
            val _texto11_1 =
              "Valor en pesos del consumo de energía eléctrica por indisponibilidad de luminarias en el nivel de tensión n"
            val _texto12 = 
              s"n:"
            val _texto12_1 =
              "Nivel de tensión 1 o 2"
            val _texto13 = 
              "TEEn:"
            val _texto13_1 =
              "Tarifa del suministro de energía eléctrica para el Servicio de Alumbrado Público en el nivel de tensión n en $/kWh"
            val _texto14 = 
              "QIj,n:"
            val _texto14_1 =
              "Carga de la luminaria j en kW, reportada con indisponibilidad en el SIAP de Servicio de Alumbrado Público"
            val _texto15 = 
              "en el nivel de tensión n. Incluye la carga de la bombilla de la luminaria y de los demás elementos internos"
            val _texto16 =
              "para su funcionamiento."
            val _texto17 = 
              "TIj,n:"
            val _texto17_1 =
              "Numero total de horas de indisponibilidad de la luminaria j, reportada en el SIAP en el nivel de tensión n,"
            val _texto18_1 =
              "Son las horas desde el momento en que se reporta la anomalía, hasta cuando el prestador del Servicio de"
            val _texto19_1 =
              "Alumbrado Público la reporta en servicio normal."
            val _texto20 =
              "m:"
            val _texto20_1 =
              "Número total de luminarias del nivel de tensión n, reportadas al registro de quejas y reclamos del SIAP,"
            val _texto21_1 =
              "del municipio o distrito durante el periodo de remuneración."
            val _texto22 =
              "Observaciones:"
            val _texto23 =
              "No se reportaron al SIAP luminarias como prendidas cuando deben estar apagadas y se evidenció que"
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font = Font(
                    bold = false,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Left
                )
              )
              .withCellValues(_texto01)
              .withHeight(new Height(40, HeightUnit.Point))

            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues("", "", "", "", "", "", "", "", "")
            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues("", "", "", "", "", "", "", "", "")

            _listRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "ITEM",
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
                "DESCRIPCION DEL MATERIAL",
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
                "UNIDAD",
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
                "CANTIDAD",
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
                "ESTADO",
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
                      // leftColor = Color.Black,
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
                "VALOR",
                Some(8),
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
              )
            )
            _listMerged += CellRange((9, 9), (5, 7))
            _listRow += com.norbitltd.spoiwo.model.Row(
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
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
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
                "OBSOLETO",
                Some(5),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = false,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thin,
                      topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thin,
                      leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thin,
                      rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "INNECESARIO",
                Some(6),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = false,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thin,
                      topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thin,
                      leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thin,
                      rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "INSERVIBLE",
                Some(7),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = false,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thin,
                      topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thin,
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
                "",
                Some(8),
                style = Some(
                  CellStyle(
                    font = Font(
                      bold = true,
                      height = new Height(10, HeightUnit.Point),
                      fontName = "Arial"
                    ),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thin,
                      topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thin,
                      leftColor = Color.Black,
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

            // Aqui van los materiales
            var _idx = 12
            _material.map {
              m =>
                _listRow += com.norbitltd.spoiwo.model.Row(
                  NumericCell(
                    _idx - 11,
                    Some(0),
                    style = Some(
                      CellStyle(
                        font = Font(
                          bold = false,
                          height = new Height(10, HeightUnit.Point),
                          fontName = "Arial"
                        ),
                        borders = CellBorders(
                          topStyle = CellBorderStyle.Thin,
                          topColor = Color.Black,
                          leftStyle = CellBorderStyle.Thick,
                          leftColor = Color.Black,
                          rightStyle = CellBorderStyle.Thin,
                          rightColor = Color.Black,
                          bottomStyle = CellBorderStyle.Thin,
                          bottomColor = Color.Black
                        ),
                        horizontalAlignment = HA.Center
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    m._3,
                    Some(1),
                    style = Some(
                      CellStyle(
                        font = Font(
                          bold = false,
                          height = new Height(10, HeightUnit.Point),
                          fontName = "Arial"
                        ),
                        borders = CellBorders(
                          topStyle = CellBorderStyle.Thin,
                          topColor = Color.Black,
                          leftStyle = CellBorderStyle.Thin,
                          leftColor = Color.Black,
                          rightStyle = CellBorderStyle.Thin,
                          rightColor = Color.Black,
                          bottomStyle = CellBorderStyle.Thin,
                          bottomColor = Color.Black
                        ),
                        horizontalAlignment = HA.Left
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
                          bold = false,
                          height = new Height(10, HeightUnit.Point),
                          fontName = "Arial"
                        ),
                        borders = CellBorders(
                          topStyle = CellBorderStyle.Thin,
                          topColor = Color.Black,
                          leftStyle = CellBorderStyle.Thin,
                          leftColor = Color.Black,
                          rightStyle = CellBorderStyle.Thin,
                          rightColor = Color.Black,
                          bottomStyle = CellBorderStyle.Thin,
                          bottomColor = Color.Black
                        ),
                        horizontalAlignment = HA.Center
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    m._4,
                    Some(3),
                    style = Some(
                      CellStyle(
                        font = Font(
                          bold = false,
                          height = new Height(10, HeightUnit.Point),
                          fontName = "Arial"
                        ),
                        borders = CellBorders(
                          topStyle = CellBorderStyle.Thin,
                          topColor = Color.Black,
                          leftStyle = CellBorderStyle.Thin,
                          leftColor = Color.Black,
                          rightStyle = CellBorderStyle.Thin,
                          rightColor = Color.Black,
                          bottomStyle = CellBorderStyle.Thin,
                          bottomColor = Color.Black
                        ),
                        horizontalAlignment = HA.Center
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    m._5,
                    Some(4),
                    style = Some(
                      CellStyle(
                        font = Font(
                          bold = false,
                          height = new Height(10, HeightUnit.Point),
                          fontName = "Arial"
                        ),
                        borders = CellBorders(
                          topStyle = CellBorderStyle.Thin,
                          topColor = Color.Black,
                          leftStyle = CellBorderStyle.Thin,
                          leftColor = Color.Black,
                          rightStyle = CellBorderStyle.Thin,
                          rightColor = Color.Black,
                          bottomStyle = CellBorderStyle.Thin,
                          bottomColor = Color.Black
                        ),
                        horizontalAlignment = HA.Center
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
                          bold = false,
                          height = new Height(10, HeightUnit.Point),
                          fontName = "Arial"
                        ),
                        borders = CellBorders(
                          topStyle = CellBorderStyle.Thin,
                          topColor = Color.Black,
                          leftStyle = CellBorderStyle.Thin,
                          leftColor = Color.Black,
                          rightStyle = CellBorderStyle.Thin,
                          rightColor = Color.Black,
                          bottomStyle = CellBorderStyle.Thin,
                          bottomColor = Color.Black
                        ),
                        horizontalAlignment = HA.Center
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
                        borders = CellBorders(
                          topStyle = CellBorderStyle.Thin,
                          topColor = Color.Black,
                          leftStyle = CellBorderStyle.Thin,
                          leftColor = Color.Black,
                          rightStyle = CellBorderStyle.Thin,
                          rightColor = Color.Black,
                          bottomStyle = CellBorderStyle.Thin,
                          bottomColor = Color.Black
                        ),
                        horizontalAlignment = HA.Center
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
                        borders = CellBorders(
                          topStyle = CellBorderStyle.Thin,
                          topColor = Color.Black,
                          leftStyle = CellBorderStyle.Thin,
                          leftColor = Color.Black,
                          rightStyle = CellBorderStyle.Thin,
                          rightColor = Color.Black,
                          bottomStyle = CellBorderStyle.Thin,
                          bottomColor = Color.Black
                        ),
                        horizontalAlignment = HA.Center
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    "",
                    Some(8),
                    style = Some(
                      CellStyle(
                        font = Font(
                          bold = false,
                          height = new Height(10, HeightUnit.Point),
                          fontName = "Arial"
                        ),
                        borders = CellBorders(
                          topStyle = CellBorderStyle.Thin,
                          topColor = Color.Black,
                          leftStyle = CellBorderStyle.Thin,
                          leftColor = Color.Black,
                          rightStyle = CellBorderStyle.Thick,
                          rightColor = Color.Black,
                          bottomStyle = CellBorderStyle.Thin,
                          bottomColor = Color.Black
                        ),
                        horizontalAlignment = HA.Center
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  )
                )
                _listMerged += CellRange((_idx - 1, _idx - 1), (1, 2))
                _idx += 1
            }
            _listRow += com.norbitltd.spoiwo.model.Row(
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
                      topColor = Color.Black
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
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
                      topColor = Color.Black
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
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
                      topColor = Color.Black
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
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
                      // leftColor = Color.Black,
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
                      topColor = Color.Black
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
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
                Some(8),
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
                      // bottomStyle = CellBorderStyle.Thin,
                      // bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )

            _idx += 1
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font = Font(
                    bold = false,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Left
                )
              )
              .withCellValues(
                "DAMOS POR RECIBIDO EL MATERIAL DESMONTADO QUE PERMANECERA BAJO CUIDADO, CUSTODIA Y RESPONSABILIDAD  DEL CONCESIONARIO HASTA TANTO EL MUNICIPIO ORDENE LO CONTRARIO, TODA VEZ QUE ÉSTE NO CUENTA CON ESPACIO ADECUADO Y SUFICIENTE PARA SU ALMACENAMIENTO"
              )
              .withHeight(new Height(30, HeightUnit.Point))
            _listMerged += CellRange((_idx - 1, _idx - 1), (0, 8))
            _idx += 1
            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues("", "", "", "", "", "", "", "", "", "")
            _idx += 1
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                style = CellStyle(
                  font = Font(
                    bold = false,
                    height = new Height(10, HeightUnit.Point),
                    fontName = "Arial"
                  ),
                  horizontalAlignment = HA.Left
                )
              )
              .withCellValues(
                s"EN CONSTANCIA FIRMAN LOS INTERESADOS EL DIA ${fecha_firma}"
              )
            _listMerged += CellRange((_idx, _idx), (0, 8))
            _idx += 1
            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues("", "", "", "", "", "", "", "", "", "")
            _idx += 1
            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues("", "", "", "", "", "", "", "", "", "")
            _idx += 1
            _listRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "",
                Some(0),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black,
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
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
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
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
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
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
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
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
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
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
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
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
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
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
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
                Some(8),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
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
                Some(8),
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
                Some(8),
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
                Some(8),
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
                gerente_nombre,
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center,
                    font = Font(
                      bold = java.lang.Boolean.TRUE
                    ),
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
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
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                almacen_nombre,
                Some(2),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center,
                    font = Font(
                      bold = java.lang.Boolean.TRUE
                    )
                  )
                ),
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
                interventor_nombre,
                Some(5),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center,
                    font = Font(
                      bold = java.lang.Boolean.TRUE
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(8),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
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
            _listMerged += CellRange((_idx - 1, _idx - 1), (0, 1))
            _listMerged += CellRange((_idx - 1, _idx - 1), (2, 4))
            _listMerged += CellRange((_idx - 1, _idx - 1), (5, 8))
            _idx += 1
            _listRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                gerente_cargo,
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
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
                Some(1),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                almacen_cargo,
                Some(2),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center
                  )
                ),
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
                interventor_cargo,
                Some(5),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(6),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(7),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(8),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
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
            _listMerged += CellRange((_idx - 1, _idx - 1), (0, 1))
            _listMerged += CellRange((_idx - 1, _idx - 1), (2, 4))
            _listMerged += CellRange((_idx - 1, _idx - 1), (5, 8))
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
                      leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                Some(8),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
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

            val _observacion =
              SQL("""SELECT STRING_AGG(distinct r1.repo_direccion,  ', ') as direccion
                  FROM siap.reporte r1
                  inner join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id and rd1.even_estado < 8
                  WHERE r1.repo_fechasolucion = {fecha_corte}
                  AND r1.reti_id = {reti_id}
                  AND r1.empr_id = {empr_id}
                  AND r1.tireuc_id = {tireuc_id}""")
                .on(
                  'fecha_corte -> _actaOpt._2,
                  'empr_id -> empr_id,
                  'reti_id -> 6,
                  'tireuc_id -> 1
                )
                .as(SqlParser.str("direccion").single)
            _listRow += com.norbitltd.spoiwo.model
              .Row(
                StringCell(
                  "OBSERVACIONES: MATERIAL DESMONTADO DE: " + _observacion,
                  Some(0),
                  style = Some(
                    CellStyle(
                      wrapText = java.lang.Boolean.TRUE,
                      borders = CellBorders(
                        // topStyle = CellBorderStyle.Thick,
                        // topColor = Color.Black,
                        leftStyle = CellBorderStyle.Thick,
                        leftColor = Color.Black,
                        // rightStyle = CellBorderStyle.Thin,
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
                      wrapText = java.lang.Boolean.TRUE,
                      borders = CellBorders(
                        topStyle = CellBorderStyle.Thick,
                        topColor = Color.Black,
                        // leftStyle = CellBorderStyle.Thick,
                        // leftColor = Color.Black,
                        // rightStyle = CellBorderStyle.Thin,
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
                      wrapText = java.lang.Boolean.TRUE,
                      borders = CellBorders(
                        topStyle = CellBorderStyle.Thick,
                        topColor = Color.Black,
                        // leftStyle = CellBorderStyle.Thick,
                        // leftColor = Color.Black,
                        // rightStyle = CellBorderStyle.Thin,
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
                      wrapText = java.lang.Boolean.TRUE,
                      borders = CellBorders(
                        topStyle = CellBorderStyle.Thick,
                        topColor = Color.Black,
                        // leftStyle = CellBorderStyle.Thick,
                        // leftColor = Color.Black,
                        // rightStyle = CellBorderStyle.Thin,
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
                      wrapText = java.lang.Boolean.TRUE,
                      borders = CellBorders(
                        topStyle = CellBorderStyle.Thick,
                        topColor = Color.Black,
                        // leftStyle = CellBorderStyle.Thick,
                        // leftColor = Color.Black,
                        // rightStyle = CellBorderStyle.Thin,
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
                      wrapText = java.lang.Boolean.TRUE,
                      borders = CellBorders(
                        topStyle = CellBorderStyle.Thick,
                        topColor = Color.Black,
                        // leftStyle = CellBorderStyle.Thick,
                        // leftColor = Color.Black,
                        // rightStyle = CellBorderStyle.Thin,
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
                      wrapText = java.lang.Boolean.TRUE,
                      borders = CellBorders(
                        topStyle = CellBorderStyle.Thick,
                        topColor = Color.Black,
                        // leftStyle = CellBorderStyle.Thick,
                        // leftColor = Color.Black,
                        // rightStyle = CellBorderStyle.Thin,
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
                      wrapText = java.lang.Boolean.TRUE,
                      borders = CellBorders(
                        topStyle = CellBorderStyle.Thick,
                        topColor = Color.Black,
                        // leftStyle = CellBorderStyle.Thick,
                        // leftColor = Color.Black,
                        // rightStyle = CellBorderStyle.Thin,
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
                  Some(8),
                  style = Some(
                    CellStyle(
                      wrapText = java.lang.Boolean.TRUE,
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
              .withHeight(new Height(30, HeightUnit.Point))
              .withStyle(
                CellStyle(
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Top
                )
              )
            _listMerged += CellRange((_idx - 1, _idx - 1), (0, 8))
            _listRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "",
                Some(0),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black
                      // rightStyle = CellBorderStyle.Thin,
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
                Some(1),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                Some(2),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thin,
                      rightColor = Color.Black
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black
                      // rightStyle = CellBorderStyle.Thin,
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
                Some(4),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                Some(5),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                Some(8),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      topStyle = CellBorderStyle.Thick,
                      topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thick,
                      rightColor = Color.Black
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
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thin,
                      rightColor = Color.Black
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                Some(8),
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
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      rightStyle = CellBorderStyle.Thin,
                      rightColor = Color.Black
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                Some(8),
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
                "Entrega",
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
                      // rightColor = Color.Black,
                      bottomStyle = CellBorderStyle.Thick,
                      bottomColor = Color.Black
                    )
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Recibe",
                Some(3),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    wrapText = java.lang.Boolean.TRUE,
                    horizontalAlignment = HA.Center,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      leftStyle = CellBorderStyle.Thick,
                      leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                    wrapText = java.lang.Boolean.TRUE,
                    borders = CellBorders(
                      // topStyle = CellBorderStyle.Thick,
                      // topColor = Color.Black,
                      // leftStyle = CellBorderStyle.Thick,
                      // leftColor = Color.Black,
                      // rightStyle = CellBorderStyle.Thin,
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
                Some(8),
                style = Some(
                  CellStyle(
                    wrapText = java.lang.Boolean.TRUE,
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
            _listMerged += CellRange((_idx, _idx), (0, 2))
            _listMerged += CellRange((_idx, _idx), (3, 8))
            _idx += 1
            _listRow.toList
          },
          mergedRegions = {
            _listMerged += CellRange((0, 0), (0, 7))
            _listMerged += CellRange((1, 1), (0, 7))
            _listMerged += CellRange((2, 2), (0, 7))
            _listMerged += CellRange((4, 4), (0, 2))
            _listMerged += CellRange((6, 6), (0, 7))

            _listMerged.toList
          },
          columns = {
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 0, width = new Width(8, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 1, width = new Width(40, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 2, width = new Width(30, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 3, width = new Width(10, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 4, width = new Width(12, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 5, width = new Width(12, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 6, width = new Width(12, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 7, width = new Width(12, WidthUnit.Character))
            _listColumn += com.norbitltd.spoiwo.model
              .Column(index = 8, width = new Width(12, WidthUnit.Character))
            _listColumn.toList
          }
        )
        var sos: ByteArrayOutputStream = new ByteArrayOutputStream()
        Workbook(sheet1).writeToOutputStream(sos)
        os = sos.toByteArray
    }
    (_actaNumero, os)
  }

  def ActaIndisponibilidadGenerador(
      anho: Int,
      periodo: Int,
      empr_id: Long,
      usua_id: Long
  ): Boolean = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val municipio_nombre = empresa.muni_descripcion.get
    val usuario = usuarioService.buscarPorId(usua_id).get
    var os = Array[Byte]()
    var _actaNumero = 0
    db.withTransaction { implicit connection =>
      // revisar si existe acta o crearla
        val _acta = SQL(
          """SELECT acin_numero FROM siap.acta_indisponibilidad WHERE acin_anho = {anho}, acin_periodo = {periodo} AND empr_id = {empr_id}"""
        ).on(
            'anho -> anho,
            'periodo -> periodo,
            'empr_id -> empr_id,
          )
          .as(SqlParser.scalar[Long].singleOpt)
        _acta match {
          case None => 
                  var _siguiente = SQL(
                    "SELECT gene_numero FROM siap.general WHERE gene_id = {gene_id}"
                  ).on(
                    'gene_id -> 12
                  ).as(SqlParser.scalar[Int].single)
                  _siguiente += 1
                  SQL(
                    "UPDATE siap.general SET gene_numero = {gene_numero} WHERE gene_id = {gene_id}"
                  ).on(
                    'gene_numero -> _siguiente,
                    'gene_id -> 12
                  )
                  .executeUpdate()
                  SQL(
                    "INSERT INTO siap.acta_indisponibilidad (acin_numero, acin_anho, acin_periodo, acin_fechagenerado, empr_id, usua_id) VALUES ({acin_numero}, {acin_anho}, {acin_periodo}, {acin_fechagenerado}, {empr_id}, {usua_id})"
                  ).on(
                    'acin_numero -> _siguiente,
                    'acin_anho -> anho,
                    'acin_periodo -> periodo,
                    'acin_fechagenerado -> new DateTime(),
                    'empr_id -> empr_id,
                    'usua_id -> usua_id
                  )
                  .executeInsert()

          case _ => None
        }
      true
    }
  }
}
