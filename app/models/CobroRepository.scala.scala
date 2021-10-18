package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream, FileInputStream}
import java.util.{Map, HashMap, Date}
import java.lang.Long
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream
import java.util.UUID.randomUUID

// Jasper
import net.sf.jasperreports.engine.JasperFillManager
import net.sf.jasperreports.engine.JasperRunManager
import net.sf.jasperreports.engine.JasperPrint
import net.sf.jasperreports.engine.JRExporter
import net.sf.jasperreports.engine.JRExporterParameter
import net.sf.jasperreports.engine.JasperCompileManager
import net.sf.jasperreports.engine.export.JRPdfExporter
import net.sf.jasperreports.export.SimpleExporterInput
import net.sf.jasperreports.export.SimpleOutputStreamExporterOutput
import net.sf.jasperreports.engine.export.ooxml.JRXlsxExporter
import net.sf.jasperreports.export.SimpleXlsxReportConfiguration
//

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str, int, double, date, scalar, flatten}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.immutable.List
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer
import scala.collection.mutable.ArrayBuffer
import scala.util.Random

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime
import org.joda.time.format.DateTimeFormat

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
import Height._
import org.apache.poi.common.usermodel.HyperlinkType
// Utility
import utilities.Utility
import utilities.N2T
import org.checkerframework.checker.units.qual.s
import org.apache.poi.ss.usermodel.CellType

case class orden_trabajo_reporte_luminaria(
    repo_id: Option[Long],
    aap_id: Option[Long]
)

case class orden_trabajo_cobro(
    otc_consecutivo: Option[Int],
    otc_tipo_operacion: Option[String],
    otc_tecnologia_anterior: Option[String],
    otc_potencia_anterior: Option[Int],
    otc_tecnologia_nueva: Option[String],
    otc_potencia_nueva: Option[Int],
    otc_luminaria_anterior: Option[String],
    otc_luminaria_nueva: Option[String],
    otc_direccion: Option[String],
    otc_cantidad: Option[Int],
    barr_id: Option[Int]
)

object orden_trabajo_cobro {
  val _set = {
    get[Option[Int]]("otc_consecutivo") ~
      get[Option[String]]("otc_tipo_operacion") ~
      get[Option[String]]("otc_tecnologia_anterior") ~
      get[Option[Int]]("otc_potencia_anterior") ~
      get[Option[String]]("otc_tecnologia_nueva") ~
      get[Option[Int]]("otc_potencia_nueva") ~
      get[Option[String]]("otc_luminaria_anterior") ~
      get[Option[String]]("otc_luminaria_nueva") ~
      get[Option[String]]("otc_direccion") ~
      get[Option[Int]]("otc_cantidad") ~
      get[Option[Int]]("barr_id") map {
      case otc_consecutivo ~
            otc_tipo_operacion ~
            otc_tecnologia_anterior ~
            otc_potencia_anterior ~
            otc_tecnologia_nueva ~
            otc_potencia_nueva ~
            otc_luminaria_anterior ~
            otc_luminaria_nueva ~
            otc_direccion ~
            otc_cantidad ~
            barr_id =>
        new orden_trabajo_cobro(
          otc_consecutivo,
          otc_tipo_operacion,
          otc_tecnologia_anterior,
          otc_potencia_anterior,
          otc_tecnologia_nueva,
          otc_potencia_nueva,
          otc_luminaria_anterior,
          otc_luminaria_nueva,
          otc_direccion,
          otc_cantidad,
          barr_id
        )
    }
  }
}

class CobroRepository @Inject()(
    dbapi: DBApi,
    usuarioService: UsuarioRepository,
    empresaService: EmpresaRepository,
    municipioService: MunicipioRepository,
    n2l: N2T
)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")
  private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"

  def siap_generar_orden_trabajo_cobro_modernizacion(
      anho: Int,
      mes: Int,
      tireuc_id: Int,
      reti_id: scala.Long,
      empr_id: scala.Long
  ): Future[Boolean] = Future {
    db.withConnection { implicit connection =>
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.set(Calendar.YEAR, anho)
      fi.set(Calendar.MONTH, mes)
      fi.set(Calendar.DATE, 1)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.set(Calendar.YEAR, anho)
      ff.set(Calendar.MONTH, mes)
      ff.set(Calendar.DATE, ff.getActualMaximum(Calendar.DATE))
      ff.set(Calendar.MILLISECOND, 59)
      ff.set(Calendar.SECOND, 59)
      ff.set(Calendar.MINUTE, 59)
      ff.set(Calendar.HOUR, 23)

      var orden_consecutivo_inicial =
        SQL("""SELECT gene_numero FROM siap.general WHERE gene_id = 5""").as(
          SqlParser.scalar[Int].single
        )

      val ordenes = SQL(
        """select 
            0 as otc_consecutivo,
            'MODERNIZACION' as otc_tipo_operacion,
	        o.otc_tecnologia_anterior, 
        	o.otc_potencia_anterior, 
        	o.otc_tecnologia_nueva, 
        	o.otc_potencia_nueva, 
	        o.otc_luminaria_anterior, 
	        o.otc_luminaria_nueva, 
	        o.otc_direccion,
            o.barr_id,
	        count(*) as otc_cantidad FROM
                (select 
	                r1.repo_consecutivo, 
                    r1.barr_id, 
                	b1.barr_descripcion as otc_direccion,
                	atc1.aatc_descripcion as otc_luminaria_anterior,
                	atc2.aatc_descripcion as otc_luminaria_nueva,
	                rdd1.aap_tecnologia_anterior as otc_tecnologia_anterior,
                	rdd1.aap_potencia_anterior as otc_potencia_anterior,  
                	rdd1.aap_tecnologia as otc_tecnologia_nueva,
                    rdd1.aap_potencia as otc_potencia_nueva, 
                	au1.aaus_descripcion
                from siap.reporte r1
                inner join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id
                inner join siap.reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id 
                inner join siap.aap a1 on a1.aap_id = rd1.aap_id
                inner join siap.aap_adicional ad1 on ad1.aap_id = a1.aap_id 
                left join siap.aap_tipo_carcasa atc1 on atc1.aatc_id = rdd1.aatc_id_anterior 
                left join siap.aap_tipo_carcasa atc2 on atc2.aatc_id = rdd1.aatc_id
                inner join siap.aap_uso au1 on au1.aaus_id = a1.aaus_id
                inner join siap.barrio b1 on b1.barr_id = r1.barr_id 
                where
                    r1.empr_id = {empr_id} and
	                r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and 
	                rd1.even_estado < 8 and
	                r1.reti_id = {reti_id}
                order by 1) o
            group by 1,2,3,4,5,6,7,8,9,10
      """
      ).on(
          'fecha_inicial -> fi.getTime(),
          'fecha_final -> ff.getTime(),
          'empr_id -> empr_id,
          'reti_id -> reti_id
        )
        .as(orden_trabajo_cobro._set *)

      val _queryReporte =
        """select distinct on (r1.repo_id) r1.repo_id, r1.tireuc_id from siap.reporte r1
                               inner join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id
                               inner join siap.reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id 
                               inner join siap.aap a1 on a1.aap_id = rd1.aap_id
                               inner join siap.aap_adicional ad1 on ad1.aap_id = a1.aap_id 
                               inner join siap.aap_uso au1 on au1.aaus_id = a1.aaus_id
                               inner join siap.barrio b1 on b1.barr_id = r1.barr_id 
                               where
                                r1.empr_id = {empr_id} and r1.rees_id = 3 and
	                            r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and 
	                            rd1.even_estado < 8 and
	                            r1.reti_id = {reti_id} and
	                            rd1.barr_id = {barr_id} and
                                rdd1.aap_tecnologia_anterior = {otc_tecnologia_anterior} and
	                            rdd1.aap_potencia_anterior = {otc_potencia_anterior} and
	                            rdd1.aap_tecnologia = {otc_tecnologia_nueva} and
	                            rdd1.aap_potencia = {otc_potencia_nueva}
                               order by 1"""
      val _queryReporteParser = int("repo_id") ~ int("tireuc_id") map {
        case a ~ b => (a, b)
      }

      ordenes.map { orden =>
        orden_consecutivo_inicial = orden_consecutivo_inicial + 1
        val _idx = SQL("""INSERT INTO siap.cobro_orden_trabajo (
                cotr_anho, 
                cotr_periodo, 
                cotr_consecutivo, 
                cotr_fecha, 
                cotr_luminaria_anterior, 
                cotr_luminaria_nueva, 
                cotr_tecnologia_anterior,
                cotr_tecnologia_nueva,
                cotr_potencia_anterior,
                cotr_potencia_nueva,
                cotr_direccion,
                cotr_cantidad,
                cotr_tipo_obra,
                cotr_tipo_obra_tipo
                ) VALUES (
                    {cotr_anho},
                    {cotr_periodo},
                    {cotr_consecutivo},
                    {cotr_fecha},
                    {cotr_luminaria_anterior},
                    {cotr_luminaria_nueva},
                    {cotr_tecnologia_anterior},
                    {cotr_tecnologia_nueva},
                    {cotr_potencia_anterior},
                    {cotr_potencia_nueva},
                    {cotr_direccion},
                    {cotr_cantidad},
                    {cotr_tipo_obra},
                    {cotr_tipo_obra_tipo}
                )""")
          .on(
            'cotr_anho -> anho,
            'cotr_periodo -> mes,
            'cotr_consecutivo -> orden_consecutivo_inicial,
            'cotr_fecha -> fi.getTime(),
            'cotr_luminaria_anterior -> orden.otc_luminaria_anterior,
            'cotr_luminaria_nueva -> orden.otc_luminaria_nueva,
            'cotr_tecnologia_anterior -> orden.otc_tecnologia_anterior,
            'cotr_tecnologia_nueva -> orden.otc_tecnologia_nueva,
            'cotr_potencia_anterior -> orden.otc_potencia_anterior,
            'cotr_potencia_nueva -> orden.otc_potencia_nueva,
            'cotr_direccion -> orden.otc_direccion,
            'cotr_cantidad -> orden.otc_cantidad,
            'cotr_tipo_obra -> "MODERNIZACION",
            'cotr_tipo_obra_tipo -> "I"
          )
          .executeInsert()
          .get

        val _reportes = SQL(_queryReporte)
          .on(
            'fecha_inicial -> fi.getTime(),
            'fecha_final -> ff.getTime(),
            'empr_id -> empr_id,
            'reti_id -> reti_id,
            'barr_id -> orden.barr_id,
            'otc_tecnologia_anterior -> orden.otc_tecnologia_anterior,
            'otc_tecnologia_nueva -> orden.otc_tecnologia_nueva,
            'otc_potencia_anterior -> orden.otc_potencia_anterior,
            'otc_potencia_nueva -> orden.otc_potencia_nueva
          )
          .as(_queryReporteParser *)

        _reportes.map { report =>
          SQL(
            """INSERT INTO siap.cobro_orden_trabajo_reporte (cotr_id, repo_id, tireuc_id) VALUES ({cotr_id}, {repo_id}, {tireuc_id})"""
          ).on(
              'cotr_id -> _idx,
              'repo_id -> report._1,
              'tireuc_id -> report._2
            )
            .executeInsert()
        }
      }
      true
    }
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_1(
      empr_id: Long,
      orden_consecutivo: String,
      orden_fecha: String,
      orden_luminaria_anterior: String,
      orden_luminaria_nueva: String,
      orden_tecnologia_anterior: String,
      orden_potencia_anterior: Int,
      orden_tecnologia_nueva: String,
      orden_potencia_nueva: Int,
      orden_cantidad: Int,
      orden_direccion: String
  ): Sheet = {
    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    val empresa = empresaService.buscarPorId(empr_id)
    Sheet(
      name = "orden",
      rows = {
        val emptyRow = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("")
        val header1 = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("ORDEN DE TRABAJO No. ITAF-" + orden_consecutivo)
        val header2 = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "OBRA DE EXPANSION CONTRATO DE CONCESION No. " + empresa.get.empr_concesion.get
          )
        val header3 = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("DEL 4 DE AGOSTO DE 2000")
        val header4 = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("ALCALDIA MUNICIPAL DE GIRON")
        val header5 = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("INTERVENTOR ALUMBRADO PUBLICO")

        val rowFecha = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("", "", "FECHA", "", "", ":", "", "", orden_fecha)

        val rowContratista = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "",
            "",
            "CONTRATISTA",
            "",
            "",
            ":",
            "",
            "",
            empresa.get.empr_descripcion
          )

        val rowNit = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "",
            "",
            "NIT",
            "",
            "",
            ":",
            "",
            "",
            empresa.get.empr_identificacion
          )

        val rowObjeto = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "",
            "",
            "OBJETO",
            "",
            "",
            ":",
            "",
            "",
            "OBRA COMPLEMENTARIA DE EXPANSION DEL ALUMBRADO PÚBLICO DEL MUNICIPIO DE GION"
          )

        val rowHeader = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "",
            "",
            "TIPO DE OPERACION",
            "TECNOLOGIA",
            "POTENCIA",
            "CANTIDAD",
            "DESCRIPCION",
            "DIRECCION"
          )

        val contentRow = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "",
            "",
            "MODERNIZACION",
            orden_tecnologia_nueva,
            orden_potencia_nueva,
            orden_cantidad,
            "MODERNIZACION DE ",
            orden_direccion
          )

        _listRow01 += emptyRow
        _listRow01 += emptyRow
        _listRow01 += emptyRow
        _listRow01 += emptyRow
        _listRow01 += emptyRow
        _listRow01 += emptyRow
        _listRow01 += emptyRow
        _listRow01 += emptyRow

        _listRow01 += header1
        _listRow01 += header2
        _listRow01 += header3
        _listRow01 += header4
        _listRow01 += header5
        _listRow01.toList
      }
    )
  }
}
