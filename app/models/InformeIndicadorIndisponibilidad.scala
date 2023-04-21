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

class InformeIndicadorIndisponibilidad @Inject()(empresaService: EmpresaRepository, usuarioService: UsuarioRepository, dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  def obtenerDatos(anho:Int, periodo:Int, empr_id: Long) = {
    var fi = new DateTime(anho, periodo, 1, 0, 0, 0, 0)
    var ff = fi.plusMonths(1).minusDays(1)

    db.withConnection { implicit connection =>
      val sql = SQL("SELECT COUNT(*) FROM siap.acta_desmonte WHERE empr_id = {empr_id} and acde_fecha between {fi} and {ff}")
        .on(
          'empr_id -> empr_id,
          'fi -> new DateTime(fi),
          'ff -> new DateTime(ff)
        )
      sql.as(scalar[scala.Long].single)
    }
  }

  def obtener(acin_id: Long, empr_id: Long, usua_id: Long): (Int, Array[Byte]) = {
    db.withConnection { implicit connection =>
      val _parser = int("acin_anho") ~ int("acin_periodo") map {
        case acin_anho ~ acin_periodo =>
          (acin_anho, acin_periodo)
      }
      val sql = SQL("SELECT * FROM siap.acta_indisponibilidad WHERE acin_id = {acin_id} and empr_id = {empr_id}")
        .on(
          'acin_id -> acin_id,
          'empr_id -> empr_id
        )
      val rango = sql.as(_parser.single)
      generarxls(rango._1, rango._2, empr_id, usua_id)
    }
  }

  def generarxls(
      anho: Int,
      periodo: Int,
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
      var actaParser = int("acde_numero") ~ get[DateTime]("acde_fecha") map {
        case acde_numero ~ acde_fecha =>
          (acde_numero, acde_fecha)
      }
      val _actaOpt = SQL(
        "SELECT acde_numero, acde_fecha FROM siap.acta_indisponibilidad WHERE acin_id = {acde_id} AND empr_id = {empr_id}"
      ).on(
          'acde_id -> anho,
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
      println("Buscando Material de Acta Numero de Id: " + anho)
      val _material = SQL(
        """"""
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

      val interventor_nombre = _firmaInterventor._1
      val interventor_cargo = _firmaInterventor._2

      val gerente_nombre = _firmaGerente._1
      val gerente_cargo = _firmaGerente._2


      var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
      var _listMerged = new ListBuffer[CellRange]()

      val sheet1 = Sheet(
          name = "Acta Indisponibilidad",
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
              .withCellValues("ACTA DE ENTREGA No." + _actaNumero)
            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues("", "", "", "", "", "", "", "", "")

            val _texto01 =
              s"EN LAS INSTALACIONES DE ${empresa.empr_descripcion} SE REUNIERON LOS INGENIEROS ${interventor_nombre} ${interventor_cargo},  ${gerente_nombre} ${gerente_cargo} Y PARA HACER ENTREGA DE LOS MATERIALES DESMONTADOS DE LOS TRABAJOS DE MODERNIZACION DEL SISTEMA ALUMBRADO PUBLICO DEL MUNICIPIO DE ${municipio_nombre}"

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
                municipio_nombre,
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
                municipio_nombre,
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

  def ActaDesmonteGenerador(
      fecha_inicial: Long,
      fecha_final: Long,
      tireuc_id: Int,
      empr_id: Long,
      usua_id: Long
  ): Boolean = {
    val empresa = empresaService.buscarPorId(empr_id).get
    val municipio_nombre = empresa.muni_descripcion.get
    val usuario = usuarioService.buscarPorId(usua_id).get
    var os = Array[Byte]()
    var _actaNumero = 0
    val fi = new DateTime(fecha_inicial)
    val ff = new DateTime(fecha_final)
    db.withTransaction { implicit connection =>
      // revisar si existe acta o crearla
      val _parseMaterial = int("elem_id") ~
        str("elem_codigo") ~
        str("elem_descripcion") ~
        str("elem_unidad") ~
        double("cantidad_retirado") map {
        case a ~ b ~ c ~ d ~ e => (a, b, c, d, e)
      }
      val _queryMaterial =
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

      val daysCount = Days.daysBetween(fi, ff).getDays() + 1
      (0 until daysCount).map(fi.plusDays(_)).foreach { fecha_corte =>
        val _fecha_corte = fecha_corte.getMillis()
        val _acta = SQL(
          """SELECT acde_numero FROM siap.acta_desmonte WHERE acde_fecha = {fecha_corte} AND empr_id = {empr_id}"""
        ).on(
            'fecha_corte -> new DateTime(_fecha_corte),
            'empr_id -> empr_id,
          )
          .as(SqlParser.scalar[Long].singleOpt)
        _acta match {
          case None => 
                val _material = SQL(_queryMaterial)
                  .on(
                    "fecha_corte" -> new DateTime(_fecha_corte),
                    "empr_id" -> empr_id,
                    "reti_id" -> 6,
                    "tireuc_id" -> tireuc_id
                  ).as(_parseMaterial *)
                if (_material.length > 0) {
                  var _siguiente = SQL(
                    "SELECT gene_numero FROM siap.general WHERE gene_id = {gene_id}"
                  ).on(
                    'gene_id -> 8
                  ).as(SqlParser.scalar[Int].single)
                  _siguiente += 1
                  SQL(
                    "UPDATE siap.general SET gene_numero = {gene_numero} WHERE gene_id = {gene_id}"
                  ).on(
                    'gene_numero -> _siguiente,
                    'gene_id -> 8
                  )
                  .executeUpdate()
                  SQL(
                    "INSERT INTO siap.acta_desmonte (acde_numero, acde_fecha, acde_fechagenerado, empr_id, usua_id) VALUES ({acde_numero}, {acde_fecha}, {acde_fechagenerado}, {empr_id}, {usua_id})"
                  ).on(
                    'acde_numero -> _siguiente,
                    'acde_fecha -> new DateTime(_fecha_corte),
                    'acde_fechagenerado -> new DateTime(),
                    'empr_id -> empr_id,
                    'usua_id -> usua_id
                  )
                  .executeInsert()
                }
          case Some(a) => None
        }
      }
      true
    }
  }
}
