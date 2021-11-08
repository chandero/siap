package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream, FileInputStream}
import java.util.{Map, HashMap, Date}
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
import anorm.SqlParser.{get, str, int, double, date, bool, scalar, flatten}
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

case class orden_trabajo_reporte(
    repo_id: Option[scala.Long],
    tireuc_id: Option[Int],
    aap_id: Option[scala.Long]
)

case class orden_trabajo_cobro(
    cotr_id: Option[scala.Long],
    cotr_anho: Option[Int],
    cotr_periodo: Option[Int],
    cotr_consecutivo: Option[Int],
    cotr_fecha: Option[DateTime],
    cotr_luminaria_anterior: Option[String],
    cotr_luminaria_nueva: Option[String],
    cotr_tecnologia_anterior: Option[String],
    cotr_tecnologia_nueva: Option[String],
    cotr_potencia_anterior: Option[Int],
    cotr_potencia_nueva: Option[Int],
    cotr_direccion: Option[String],
    cotr_cantidad: Option[Int],
    cotr_tipo_obra: Option[scala.Long],
    cotr_tipo_obra_tipo: Option[String],
    empr_id: Option[scala.Long],
    tireuc_id: Option[Int],
    barr_id: Option[scala.Long]
)

object orden_trabajo_cobro {
  val _set = {
    get[Option[scala.Long]]("cotr_id") ~
      get[Option[Int]]("cotr_anho") ~
      get[Option[Int]]("cotr_periodo") ~
      get[Option[Int]]("cotr_consecutivo") ~
      get[Option[DateTime]]("cotr_fecha") ~
      get[Option[String]]("cotr_luminaria_anterior") ~
      get[Option[String]]("cotr_luminaria_nueva") ~
      get[Option[String]]("cotr_tecnologia_anterior") ~
      get[Option[String]]("cotr_tecnologia_nueva") ~
      get[Option[Int]]("cotr_potencia_anterior") ~
      get[Option[Int]]("cotr_potencia_nueva") ~
      get[Option[String]]("cotr_direccion") ~
      get[Option[Int]]("cotr_cantidad") ~
      get[Option[scala.Long]]("cotr_tipo_obra") ~
      get[Option[String]]("cotr_tipo_obra_tipo") ~
      get[Option[scala.Long]]("empr_id") ~
      get[Option[Int]]("tireuc_id") ~
      get[Option[scala.Long]]("barr_id") map {
      case cotr_id ~
            cotr_anho ~
            cotr_periodo ~
            cotr_consecutivo ~
            cotr_fecha ~
            cotr_luminaria_anterior ~
            cotr_luminaria_nueva ~
            cotr_tecnologia_anterior ~
            cotr_tecnologia_nueva ~
            cotr_potencia_anterior ~
            cotr_potencia_nueva ~
            cotr_direccion ~
            cotr_cantidad ~
            cotr_tipo_obra ~
            cotr_tipo_obra_tipo ~
            empr_id ~
            tireuc_id ~
            barr_id =>
        new orden_trabajo_cobro(
          cotr_id,
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
          cotr_tipo_obra_tipo,
          empr_id,
          tireuc_id,
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
  private var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
  private var _listMerged01 = new ListBuffer[CellRange]()
  private var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]() 
  private var _listSubTotal = new ArrayBuffer[String]()
  private var _listTotal = new ArrayBuffer[String]()
  private var _idx = 0

  

  def buscarPorId(empr_id: Long, cotr_id: Long): Option[orden_trabajo_cobro] = {
    db.withConnection { implicit connection =>
      val query =
        "SELECT * FROM siap.cobro_orden_trabajo WHERE empr_id = {empr_id} and cotr_id = {cotr_id}"
      SQL(query)
        .on(
          'empr_id -> empr_id,
          'cotr_id -> cotr_id
        )
        .as(orden_trabajo_cobro._set.singleOpt)
    }
  }

  def siap_obtener_orden_trabajo_cobro_modernizacion(
      empr_id: Long,
      reti_id: Long
  ): Future[Iterable[orden_trabajo_cobro]] = Future {
    db.withConnection { implicit connection =>
      val query =
        """SELECT * FROM siap.cobro_orden_trabajo WHERE empr_id = {empr_id} and cotr_tipo_obra = {reti_id}"""
      val _lista = SQL(query)
        .on(
          'empr_id -> empr_id,
          'reti_id -> reti_id
        )
        .as(orden_trabajo_cobro._set *)
      _lista
    }
  }

  def siap_generar_orden_trabajo_cobro_modernizacion(
      anho: Int,
      mes: Int,
      tireuc_id: Int,
      reti_id: scala.Long,
      empr_id: scala.Long,
      cotr_consecutivo: scala.Long
  ): Future[Boolean] = Future {
    db.withConnection { implicit connection =>
      var fi = Calendar.getInstance()
      var ff = Calendar.getInstance()
      fi.set(Calendar.YEAR, anho)
      fi.set(Calendar.MONTH, mes - 1)
      fi.set(Calendar.DATE, 1)
      fi.set(Calendar.MILLISECOND, 0)
      fi.set(Calendar.SECOND, 0)
      fi.set(Calendar.MINUTE, 0)
      fi.set(Calendar.HOUR, 0)

      ff.setTimeInMillis(fi.getTimeInMillis())
      val lastDay = ff.getActualMaximum(Calendar.DATE)
      println("Ultimo día:" + lastDay)
      ff.set(Calendar.DAY_OF_MONTH, lastDay)

      SQL("UPDATE siap.general SET gene_numero = {numero} WHERE gene_id = 5")
        .on(
          'numero -> (cotr_consecutivo - 1)
        )
        .executeUpdate()

      var orden_consecutivo_inicial =
        SQL("""SELECT gene_numero FROM siap.general WHERE gene_id = 5""").as(
          SqlParser.scalar[Int].single
        )
      println("Fecha Inicial: " + fi.getTime())
      println("Fecha Final: " + ff.getTime())
      val _parseOrden = int("barr_id") ~ str("cotr_direccion") ~ str(
        "cotr_tecnologia_anterior"
      ) ~ int("cotr_potencia_anterior") ~
        str("cotr_tecnologia_nueva") ~ int("cotr_potencia_nueva") ~ str(
        "cotr_luminaria_anterior"
      ) ~ str("cotr_luminaria_nueva") ~
        int("repo_id") ~ int("repo_consecutivo") ~ int("aap_id") ~ str(
        "aaus_descripcion"
      ) ~ bool("is_orden") map {
        case a1 ~ a2 ~ a3 ~ a4 ~ a5 ~ a6 ~ a7 ~ a8 ~ a9 ~ a10 ~ a11 ~ a12 ~ a13 =>
          (a1, a2, a3, a4, a5, a6, a7, a8, a9, a10, a11, a12, a13)
      }
      val ordenes = SQL(
        """select 
                  r1.barr_id as barr_id, 
                	b1.barr_descripcion as cotr_direccion,
	                rdd1.aap_tecnologia_anterior as cotr_tecnologia_anterior,
                	rdd1.aap_potencia_anterior as cotr_potencia_anterior,  
                	rdd1.aap_tecnologia as cotr_tecnologia_nueva,
                  rdd1.aap_potencia as cotr_potencia_nueva, 
                	atc1.aatc_descripcion as cotr_luminaria_anterior,
                	atc2.aatc_descripcion as cotr_luminaria_nueva,
					        r1.repo_id as repo_id,
	                r1.repo_consecutivo as repo_consecutivo, 
					        rd1.aap_id as aap_id,	
                	au1.aaus_descripcion as aaus_descripcion,
                	false as is_orden
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
                    r1.empr_id = {empr_id} and r1.rees_id = 3 and
	                r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and 
	                rd1.even_estado < 8 and
	                r1.reti_id = {reti_id}
                union all
                select
                    r1.barr_id, 
                  	b1.barr_descripcion as cotr_direccion,
  	                rdd1.aap_tecnologia_anterior as cotr_tecnologia_anterior,
                  	rdd1.aap_potencia_anterior as cotr_potencia_anterior,  
                  	rdd1.aap_tecnologia as cotr_tecnologia_nueva,
                    rdd1.aap_potencia as cotr_potencia_nueva, 
                  	max(atc1.aatc_descripcion) as cotr_luminaria_anterior,
                  	max(atc2.aatc_descripcion) as cotr_luminaria_nueva,
					          count(*) as repo_id,
	                  count(*) as repo_consecutivo,
	                  count(*) as aap_id,                	
                	  max(au1.aaus_descripcion) as aaus_descripcion,
                	  true as is_orden
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
                    r1.empr_id = {empr_id} and r1.rees_id = 3 and
	                r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and 
	                rd1.even_estado < 8 and
	                r1.reti_id = {reti_id}
                group by 1,2,3,4,5,6
                order by  barr_id, 
                          cotr_direccion,
	                        cotr_tecnologia_anterior,
                          cotr_potencia_anterior,  
                          cotr_tecnologia_nueva,
                          cotr_potencia_nueva, 
                          is_orden desc
      """
      ).on(
          'fecha_inicial -> fi.getTime(),
          'fecha_final -> ff.getTime(),
          'empr_id -> empr_id,
          'reti_id -> reti_id,
          'anho -> anho,
          'mes -> mes
        )
        .as(_parseOrden *)

      /*      val _queryReporte =
        """select distinct r1.repo_id, r1.tireuc_id, rdd1.aap_id from siap.reporte r1
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
	                              r1.barr_id = {barr_id} and
                                rdd1.aap_tecnologia_anterior = {cotr_tecnologia_anterior} and
	                              rdd1.aap_potencia_anterior = {cotr_potencia_anterior} and
	                              rdd1.aap_tecnologia = {cotr_tecnologia_nueva} and
	                              rdd1.aap_potencia = {cotr_potencia_nueva}
                               order by 1,3"""
      val _queryReporteParser = int("repo_id") ~ int("tireuc_id") ~ int("aap_id") map {
        case a ~ b ~ c => (a, b, c)
      }*/

      var _idx = 0L;
      ordenes.map { orden =>
        if (orden._13) {
          orden_consecutivo_inicial = orden_consecutivo_inicial + 1
          _idx = SQL("""INSERT INTO siap.cobro_orden_trabajo (
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
                cotr_tipo_obra_tipo,
                empr_id,
                tireuc_id,
                barr_id
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
                    {cotr_tipo_obra_tipo},
                    {empr_id},
                    {tireuc_id},
                    {barr_id}
                )""")
            .on(
              'cotr_anho -> anho,
              'cotr_periodo -> mes,
              'cotr_consecutivo -> orden_consecutivo_inicial,
              'cotr_fecha -> fi.getTime(),
              'cotr_luminaria_anterior -> orden._7,
              'cotr_luminaria_nueva -> orden._8,
              'cotr_tecnologia_anterior -> orden._3,
              'cotr_tecnologia_nueva -> orden._5,
              'cotr_potencia_anterior -> orden._4,
              'cotr_potencia_nueva -> orden._6,
              'cotr_direccion -> orden._2,
              'cotr_cantidad -> orden._10,
              'cotr_tipo_obra -> 6,
              'cotr_tipo_obra_tipo -> "I",
              'empr_id -> empr_id,
              'tireuc_id -> 1,
              'barr_id -> orden._1
            )
            .executeInsert()
            .get
          println(
            "Insertanto orden _idx:" + _idx + ", consecutivo:" + orden_consecutivo_inicial
          )
        } else {
          SQL(
            """INSERT INTO siap.cobro_orden_trabajo_reporte (cotr_id, repo_id, tireuc_id, aap_id) VALUES ({cotr_id}, {repo_id}, {tireuc_id}, {aap_id})"""
          ).on(
              'cotr_id -> _idx,
              'repo_id -> orden._9,
              'tireuc_id -> 1,
              'aap_id -> orden._11
            )
            .executeInsert()
          println(
            "Insertanto reporte luminaria _idx:" + _idx + ", repo_id:" + orden._9 + ", app_id:" + orden._11
          )
        }
      }
      // Obtener Materiales
      val _parseMaterial = int("cotr_id") ~ int("elem_id") ~ double(
        "elpr_precio"
      ) ~ double("elem_cantidad") map {
        case a1 ~ a2 ~ a3 ~ a4 => (a1, a2, a3, a4)
      }
      val _queryMaterial =
        """select cotr1.cotr_id, e1.elem_id, CASE WHEN ep1.elpr_precio IS NULL THEN 0.0 ELSE ep1.elpr_precio END as elpr_precio, SUM(re1.even_cantidad_instalado) as elem_cantidad
                                from siap.cobro_orden_trabajo cotr1
                                inner join siap.cobro_orden_trabajo_reporte cotrr1 on cotrr1.cotr_id = cotr1.cotr_id
                                inner join siap.reporte r1 on r1.repo_id = cotrr1.repo_id
                                inner join siap.reporte_evento re1 on re1.repo_id = r1.repo_id and re1.aap_id = cotrr1.aap_id
                                inner join siap.elemento e1 on e1.elem_id = re1.elem_id 
                                left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = {anho}
                                where cotr1.cotr_fecha between {fecha_inicial} and {fecha_final} and cotr1.empr_id = {empr_id} and cotr1.cotr_tipo_obra = {reti_id} and re1.even_estado < 8
                                group by 1,2,3
                                order by 1,2"""
      val _materiales = SQL(_queryMaterial)
        .on(
          'empr_id -> empr_id,
          'anho -> anho,
          'reti_id -> reti_id,
          'fecha_inicial -> fi.getTime(),
          'fecha_final -> ff.getTime()
        )
        .as(_parseMaterial *)

      val _queryInsertarMaterial =
        """INSERT INTO siap.cobro_orden_trabajo_material (cotr_id, elem_id, cotrma_valor_unitario, cotrma_cantidad) VALUES ({cotr_id}, {elem_id}, {cotrma_valor_unitario}, {cotrma_cantidad})"""
      _materiales.map { material =>
        SQL(_queryInsertarMaterial)
          .on(
            'cotr_id -> material._1,
            'elem_id -> material._2,
            'cotrma_valor_unitario -> material._3,
            'cotrma_cantidad -> material._4
          )
          .executeInsert()
      }
      /// ACTUALIZAR CONSECUTIVO
      SQL("UPDATE siap.general SET gene_numero = {numero} WHERE gene_id = 5")
        .on(
          'numero -> (orden_consecutivo_inicial)
        )
        .executeUpdate()
      true
    }
  }

  def siap_orden_trabajo_cobro(
      empr_id: Long,
      cotr_id: Long
  ): (Int, Array[Byte]) = {
    import Height._
    val empresa = empresaService.buscarPorId(empr_id)
    val orden = buscarPorId(empr_id, cotr_id)

    val sheet1 = siap_orden_trabajo_cobro_modernizacion_hoja_1_orden(
      empresa.get,
      orden.get
    )
    val sheet3 = siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios(
      empresa.get,
      orden.get
    )
    val sheet4 = siap_orden_trabajo_cobro_modernizacion_hoja_4_reporte_luminaria(empresa.get, orden.get)
    println("Escribiendo en el Stream")
    var os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Workbook(sheet1, sheet3, sheet4).writeToOutputStream(os)
    println("Stream Listo")
    (orden.get.cotr_consecutivo.get, os.toByteArray)
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_1_orden(
      empresa: Empresa,
      orden: orden_trabajo_cobro
  ): Sheet = {
    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()

    Sheet(
      name = "orden",
      rows = {
        val emptyRow = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("")
        val header1 = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "ORDEN DE TRABAJO No. ITAF-" + orden.cotr_consecutivo.get
          )
        val header2 = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "OBRA DE EXPANSION CONTRATO DE CONCESION No. " + empresa.empr_concesion.get
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
          .withCellValues(
            "",
            "",
            "FECHA",
            "",
            "",
            ":",
            "",
            "",
            orden.cotr_fecha.get.toString("dd/MM/yyyy")
          )

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
            empresa.empr_descripcion
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
            empresa.empr_identificacion
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
            orden.cotr_tecnologia_nueva.get,
            orden.cotr_potencia_nueva.get,
            orden.cotr_cantidad.get,
            "MODERNIZACION DE ",
            orden.cotr_direccion.get
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

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios(
      empresa: Empresa,
      orden: orden_trabajo_cobro
  ): Sheet = {
    db.withConnection { implicit connection =>
      val fuenteTotal = Font(
        height = 12.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = true,
        strikeout = false
      )
      val fuenteSubTotal = Font(
        height = 11.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = true,
        strikeout = false
      )
      val fuenteCantidadTotal = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = true,
        strikeout = false
      )
      val fuenteNoBold = Font(
        height = 12.points,
        fontName = "Liberation Sans",
        bold = false,
        italic = false,
        strikeout = false
      )
      val colorSubTotal = Color(255, 182, 108)
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_101(empresa, orden)
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_102(empresa, orden)
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_103(empresa, orden)            
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_104(empresa, orden)            
      /* 
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_105(empresa, orden)            
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_106(empresa, orden)            
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_107(empresa, orden)            
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_108(empresa, orden)            
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_109(empresa, orden)            
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_110(empresa, orden)            
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_111(empresa, orden)            
      siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_112(empresa, orden)            
      */
      Sheet(
        name = "Unitarios",
        rows = { 
          _listRow01.toList
        },
        mergedRegions = {
          _listMerged01.toList
        },
        columns = {
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 6, width = new Width(18, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 7, width = new Width(18, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 8, width = new Width(18, WidthUnit.Character))

          _listColumn.toList
        }        
      )
    }
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_101(empresa: Empresa, orden: orden_trabajo_cobro) {
    db.withConnection { implicit connection =>
          val emptyRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("")

          val headerRow01 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", empresa.empr_descripcion)
          val headerRow02 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", empresa.muni_descripcion.get)
          val headerRow03 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", "ANALISIS DE PRECIOS UNITARIOS")
          val headerRow04 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "Item",
              "1.01"
            )
          val headerRow05 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Obra",
              "",
              "MODERNIZACION A LED",
              "",
              "",
              "",
              "",
              "",
              "Und",
              "UND"
            )
          val headerRow06 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Area",
              "",
              orden.cotr_direccion.get,
              "",
              "",
              "",
              "",
              "",
              "Cant",
              orden.cotr_cantidad.get
            )
          val headerRow07 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Actividad",
              "",
              "SUMINISTRO E INSTALACION DE LUMINARIA " + orden.cotr_luminaria_nueva.get + " " + orden.cotr_tecnologia_nueva.get + " " + orden.cotr_potencia_nueva.get + "W",
              "",
              "",
              "",
              "",
              "",
              "",
              ""
            )
          val headerRow08 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "CANT",
              "TARIFA / DIA",
              "RENDIMIENTO",
              "VALOR PARCIAL",
              "",
              ""
            )
          val headerRow09 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "TRANSPORTE, EQUIPOS Y HERRAMIENTAS",
              "",
              "",
              "",
              "",
              "INST / EQUIPO",
              "UNID/DIA/EQU.",
              "",
              "",
              ""
            )
          val headerRow10 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "(a)",
              "(b)",
              "(c)",
              "a x b / c",
              "",
              ""
            )

          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow01
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow02
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow03
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += headerRow04
          _idx += 1
          _listRow01 += headerRow05
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += headerRow06
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += headerRow07
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow08
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += headerRow09
           _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += headerRow10
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          /// AQUI DEBE IR LA INFO DE TRANSPORTE, EQUIPO Y HERRAMIENTA
          var _herramientaLista = new ArrayBuffer[(String, Long, Long, Long)]()
          _herramientaLista += (("HERRAMIENTA MENOR", 2, 2647, 1))
          _herramientaLista += (("CAMIONETA", 1, 158908,8))
          var _idx_inicial = _idx + 1
          var _idx_final = _idx + 1          
          _herramientaLista.map { _m =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2,
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3,
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._4,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),                
                FormulaCell(
                  "SUM(F" + (_idx + 1) + "*G" + (_idx + 1) + "/H" + (_idx + 1) +")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1              
            }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (A)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "UND",
              "PRECIO POR",
              "CANTIDAD DE",
              "VALOR",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "MATERIALES",
              "",
              "",
              "",
              "",
              "UNIDAD",
              "MATERIAL/UND",
              "PARCIAL",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          val headerRow11 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "",
              "(d)",
              "(e)",
              "d x e",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listRow01 += headerRow11
          _idx += 1
          val _parseMaterial = str("elem_descripcion") ~ get[Option[String]](
            "elpr_unidad"
          ) ~ get[Option[Int]]("elpr_precio") ~ double("cotrma_cantidad") map {
            case a1 ~ a2 ~ a3 ~ a4 => (a1, a2, a3, a4)
          }
          val _materiales = SQL(
            """select distinct e1.elem_descripcion, ep1.elpr_unidad, ep1.elpr_precio, cotm1.cotrma_cantidad from siap.cobro_orden_trabajo cotr1
                                 inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cotr1.cotr_id 
                                 inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                                 inner join siap.elemento_unitario eu1 on eu1.elem_id = e1.elem_id
                                 inner join siap.unitario u1 on u1.unit_id = eu1.unit_id
                                 left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cotr1.cotr_fecha)
                                 where cotr1.empr_id = {empr_id} and cotr1.cotr_id = {cotr_id} AND u1.unit_codigo = {unit_codigo}"""
          ).on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> "1.01"
            )
            .as(_parseMaterial *)
          _idx_inicial = _idx + 1
          _idx_final = _idx + 1
          _materiales.map {
            _m =>
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  _m._2 match {
                    case Some(value) => value
                    case None        => ""
                  },
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3 match {
                    case Some(value) => value
                    case None        => 0
                  },
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._4,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(G" + (_idx + 1) + "*H" + (_idx + 1) + ")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1
          }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (B)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          // MANO OBRA
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "CANT",
              "SALARIO",
              "RENDIMIENTO",
              "VALOR",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))            
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "MANO DE OBRA",
              "",
              "",
              "",
              "",
              "INTEGRAL / DIA",
              "UNID / DIA",
              "PARCIAL",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              // _listMerged01 += CellRange((_idx, _idx), (6, 7))
              // _listMerged01 += CellRange((_idx, _idx), (8, 9))
              // _listMerged01 += CellRange((_idx, _idx), (10, 11))
              // _listMerged01 += CellRange((_idx, _idx), (12, 13))            
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "(f)",
              "(g)",
              "(h)",
              "(f x g / h)",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
            _idx += 1            
          //
          // LEER MANO DE OBRA
          val _parseManoObra = str("maob_descripcion") ~ get[Option[Int]]("maobpr_precio") ~ double("maobpr_rendimiento") map { case a1 ~ a2 ~ a3 => (a1, a2, a3) }
          val _manoObra = SQL("""SELECT mob1.maob_descripcion, mop1.maobpr_precio, mop1.maobpr_rendimiento from siap.mano_obra mob1
                                 INNER JOIN siap.mano_obra_precio mop1 ON mop1.maob_id = mob1.maob_id
                                 WHERE mop1.maobpr_anho = {anho}""").
                                 on(
                                   'anho -> orden.cotr_anho.get
                                 ).as(_parseManoObra *)
          _idx_inicial = _idx + 1
          _idx_final = _idx + 1
          _manoObra.map { _m => 
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  1,
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2 match {
                    case Some(value) => value
                    case None        => 0
                  },
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(F" + (_idx + 1) + "* G" + (_idx + 1) + "/ H" + (_idx + 1) + ")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1            
          }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (C)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "SUBTOTAL SUMINISTRO Y MONTAJE",
                Some(6),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet               
              ),
              FormulaCell(
                "SUM(" + _listSubTotal.mkString(",") + ")",
                Some(9),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("#,##0")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
            _listMerged01 += CellRange((_idx, _idx), (1, 5))
            _listMerged01 += CellRange((_idx, _idx), (6, 8))
            _idx_final = _idx
            //_listMerged01 += CellRange((_idx, _idx), (12, 13))
            _idx += 1
          /// AQUI DEBE IR LA INFO DE INGENIERIA
          var _ingLista = new ArrayBuffer[(String, Double)]()
          _ingLista += (("INGENIERIA", 0.05))
          _ingLista += (("ADMINISTRACION DE LA OBRA", 0.28))
          _ingLista += (("INSPECTORES DE OBRA", 0.02))
          _idx_inicial = _idx + 1
          var _idx_subtotal = _idx
          _listSubTotal = new ArrayBuffer[String]()
          _listSubTotal += ("I"+ _idx_subtotal)
          _ingLista.map { _m =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2,
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("##0.00%")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),          
                FormulaCell(
                  "SUM(J" + (_idx_subtotal ) + "*I" + (_idx + 1) + ")",
                  Some(9),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 5))
              _listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _listSubTotal += ("J" + (_idx + 1))
              _idx += 1
              _idx_final += 1              
            }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "TOTAL PRECIO UNIDAD CONSTRUCTIVA",
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(" + _listSubTotal.mkString(",") + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listRow01 += emptyRow
          _listRow01 += emptyRow
          _idx = _idx + 3
    }
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_102(empresa: Empresa, orden: orden_trabajo_cobro) {
    db.withConnection { implicit connection =>
          val emptyRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("")

          val headerRow01 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", empresa.empr_descripcion)
          val headerRow02 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", empresa.muni_descripcion.get)
          val headerRow03 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", "ANALISIS DE PRECIOS UNITARIOS")
          val headerRow04 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "Item",
              "1.02"
            )
          val headerRow05 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Obra",
              "",
              "MODERNIZACION A LED",
              "",
              "",
              "",
              "",
              "",
              "Und",
              "UND"
            )
          val headerRow06 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Area",
              "",
              orden.cotr_direccion.get,
              "",
              "",
              "",
              "",
              "",
              "Cant",
              orden.cotr_cantidad.get
            )
          val headerRow07 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Actividad",
              "",
              "SUMINISTRO E INSTALACION DE POSTES" ,
              "",
              "",
              "",
              "",
              "",
              "",
              ""
            )
          val headerRow08 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "CANT",
              "TARIFA / DIA",
              "RENDIMIENTO",
              "VALOR PARCIAL",
              "",
              ""
            )
          val headerRow09 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "TRANSPORTE, EQUIPOS Y HERRAMIENTAS",
              "",
              "",
              "",
              "",
              "INST / EQUIPO",
              "UNID/DIA/EQU.",
              "",
              "",
              ""
            )
          val headerRow10 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "(a)",
              "(b)",
              "(c)",
              "a x b / c",
              "",
              ""
            )

          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow01
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow02
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow03
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += headerRow04
          _idx += 1
          _listRow01 += headerRow05
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += headerRow06
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += headerRow07
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow08
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += headerRow09
           _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += headerRow10
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          /// AQUI DEBE IR LA INFO DE TRANSPORTE, EQUIPO Y HERRAMIENTA
          var _herramientaLista = new ArrayBuffer[(String, Long, Long, Long)]()
          _herramientaLista += (("HERRAMIENTA MENOR", 2, 2647, 1))
          _herramientaLista += (("CAMIONETA", 1, 158908,8))
          var _idx_inicial = _idx + 1
          var _idx_final = _idx + 1          
          _herramientaLista.map { _m =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2,
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3,
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._4,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),                
                FormulaCell(
                  "SUM(F" + (_idx + 1) + "*G" + (_idx + 1) + "/H" + (_idx + 1) +")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1              
            }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (A)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "UND",
              "PRECIO POR",
              "CANTIDAD DE",
              "VALOR",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "MATERIALES",
              "",
              "",
              "",
              "",
              "UNIDAD",
              "MATERIAL/UND",
              "PARCIAL",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          val headerRow11 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "",
              "(d)",
              "(e)",
              "d x e",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listRow01 += headerRow11
          _idx += 1
          val _parseMaterial = str("elem_descripcion") ~ get[Option[String]](
            "elpr_unidad"
          ) ~ get[Option[Int]]("elpr_precio") ~ double("cotrma_cantidad") map {
            case a1 ~ a2 ~ a3 ~ a4 => (a1, a2, a3, a4)
          }
          val _materiales = SQL(
            """select distinct e1.elem_descripcion, ep1.elpr_unidad, ep1.elpr_precio, cotm1.cotrma_cantidad from siap.cobro_orden_trabajo cotr1
                                 inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cotr1.cotr_id 
                                 inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                                 inner join siap.elemento_unitario eu1 on eu1.elem_id = e1.elem_id
                                 inner join siap.unitario u1 on u1.unit_id = eu1.unit_id
                                 left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cotr1.cotr_fecha)
                                 where cotr1.empr_id = {empr_id} and cotr1.cotr_id = {cotr_id} AND u1.unit_codigo = {unit_codigo}"""
          ).on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> "1.02"
            )
            .as(_parseMaterial *)
          _idx_inicial = _idx + 1
          _idx_final = _idx + 1
          _materiales.map {
            _m =>
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  _m._2 match {
                    case Some(value) => value
                    case None        => ""
                  },
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3 match {
                    case Some(value) => value
                    case None        => 0
                  },
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._4,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(G" + (_idx + 1) + "*H" + (_idx + 1) + ")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1
          }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (B)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          // MANO OBRA
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "CANT",
              "SALARIO",
              "RENDIMIENTO",
              "VALOR",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))            
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "MANO DE OBRA",
              "",
              "",
              "",
              "",
              "INTEGRAL / DIA",
              "UNID / DIA",
              "PARCIAL",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              // _listMerged01 += CellRange((_idx, _idx), (6, 7))
              // _listMerged01 += CellRange((_idx, _idx), (8, 9))
              // _listMerged01 += CellRange((_idx, _idx), (10, 11))
              // _listMerged01 += CellRange((_idx, _idx), (12, 13))            
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "(f)",
              "(g)",
              "(h)",
              "(f x g / h)",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
            _idx += 1            
          //
          // LEER MANO DE OBRA
          val _parseManoObra = str("maob_descripcion") ~ get[Option[Int]]("maobpr_precio") ~ double("maobpr_rendimiento") map { case a1 ~ a2 ~ a3 => (a1, a2, a3) }
          val _manoObra = SQL("""SELECT mob1.maob_descripcion, mop1.maobpr_precio, mop1.maobpr_rendimiento from siap.mano_obra mob1
                                 INNER JOIN siap.mano_obra_precio mop1 ON mop1.maob_id = mob1.maob_id
                                 WHERE mop1.maobpr_anho = {anho}""").
                                 on(
                                   'anho -> orden.cotr_anho.get
                                 ).as(_parseManoObra *)
          _idx_inicial = _idx + 1
          _idx_final = _idx + 1
          _manoObra.map { _m => 
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  1,
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2 match {
                    case Some(value) => value
                    case None        => 0
                  },
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(F" + (_idx + 1) + "* G" + (_idx + 1) + "/ H" + (_idx + 1) + ")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1            
          }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (C)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "SUBTOTAL SUMINISTRO Y MONTAJE",
                Some(6),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet               
              ),
              FormulaCell(
                "SUM(" + _listSubTotal.mkString(",") + ")",
                Some(9),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("#,##0")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
            _listMerged01 += CellRange((_idx, _idx), (1, 5))
            _listMerged01 += CellRange((_idx, _idx), (6, 8))
            _idx_final = _idx
            //_listMerged01 += CellRange((_idx, _idx), (12, 13))
            _idx += 1
          /// AQUI DEBE IR LA INFO DE INGENIERIA
          var _ingLista = new ArrayBuffer[(String, Double)]()
          _ingLista += (("INGENIERIA", 0.05))
          _ingLista += (("ADMINISTRACION DE LA OBRA", 0.28))
          _ingLista += (("INSPECTORES DE OBRA", 0.02))
          _idx_inicial = _idx + 1
          var _idx_subtotal = _idx
          _listSubTotal = new ArrayBuffer[String]()
          _listSubTotal += ("I"+ _idx_subtotal)
          _ingLista.map { _m =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2,
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("##0.00%")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),          
                FormulaCell(
                  "SUM(J" + (_idx_subtotal ) + "*I" + (_idx + 1) + ")",
                  Some(9),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 5))
              _listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _listSubTotal += ("J" + (_idx + 1))
              _idx += 1
              _idx_final += 1              
            }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "TOTAL PRECIO UNIDAD CONSTRUCTIVA",
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(" + _listSubTotal.mkString(",") + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listRow01 += emptyRow
          _listRow01 += emptyRow
          _idx = _idx + 3
    }
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_103(empresa: Empresa, orden: orden_trabajo_cobro) {
    db.withConnection { implicit connection =>
          val emptyRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("")

          val headerRow01 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", empresa.empr_descripcion)
          val headerRow02 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", empresa.muni_descripcion.get)
          val headerRow03 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", "ANALISIS DE PRECIOS UNITARIOS")
          val headerRow04 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "Item",
              "1.03"
            )
          val headerRow05 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Obra",
              "",
              "MODERNIZACION A LED",
              "",
              "",
              "",
              "",
              "",
              "Und",
              "GLB"
            )
          val headerRow06 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Area",
              "",
              orden.cotr_direccion.get,
              "",
              "",
              "",
              "",
              "",
              "Cant",
              orden.cotr_cantidad.get
            )
          val headerRow07 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Actividad",
              "",
              "SUMINISTRO E INSTALACION DE CANALIZACION DE REDES" ,
              "",
              "",
              "",
              "",
              "",
              "",
              ""
            )
          val headerRow08 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "CANT",
              "TARIFA / DIA",
              "RENDIMIENTO",
              "VALOR PARCIAL",
              "",
              ""
            )
          val headerRow09 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "TRANSPORTE, EQUIPOS Y HERRAMIENTAS",
              "",
              "",
              "",
              "",
              "INST / EQUIPO",
              "UNID/DIA/EQU.",
              "",
              "",
              ""
            )
          val headerRow10 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "(a)",
              "(b)",
              "(c)",
              "a x b / c",
              "",
              ""
            )

          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow01
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow02
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow03
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += headerRow04
          _idx += 1
          _listRow01 += headerRow05
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += headerRow06
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += headerRow07
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow08
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += headerRow09
           _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += headerRow10
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          /// AQUI DEBE IR LA INFO DE TRANSPORTE, EQUIPO Y HERRAMIENTA
          var _herramientaLista = new ArrayBuffer[(String, Long, Long, Long)]()
          _herramientaLista += (("HERRAMIENTA MENOR", 2, 2647, 1))
          _herramientaLista += (("CAMIONETA", 1, 158908,8))
          var _idx_inicial = _idx + 1
          var _idx_final = _idx + 1          
          _herramientaLista.map { _m =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2,
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3,
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._4,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),                
                FormulaCell(
                  "SUM(F" + (_idx + 1) + "*G" + (_idx + 1) + "/H" + (_idx + 1) +")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1              
            }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (A)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "UND",
              "PRECIO POR",
              "CANTIDAD DE",
              "VALOR",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "MATERIALES",
              "",
              "",
              "",
              "",
              "UNIDAD",
              "MATERIAL/UND",
              "PARCIAL",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          val headerRow11 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "",
              "(d)",
              "(e)",
              "d x e",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listRow01 += headerRow11
          _idx += 1
          val _parseMaterial = str("elem_descripcion") ~ get[Option[String]](
            "elpr_unidad"
          ) ~ get[Option[Int]]("elpr_precio") ~ double("cotrma_cantidad") map {
            case a1 ~ a2 ~ a3 ~ a4 => (a1, a2, a3, a4)
          }
          val _materiales = SQL(
            """select distinct e1.elem_descripcion, ep1.elpr_unidad, ep1.elpr_precio, cotm1.cotrma_cantidad from siap.cobro_orden_trabajo cotr1
                                 inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cotr1.cotr_id 
                                 inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                                 inner join siap.elemento_unitario eu1 on eu1.elem_id = e1.elem_id
                                 inner join siap.unitario u1 on u1.unit_id = eu1.unit_id
                                 left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cotr1.cotr_fecha)
                                 where cotr1.empr_id = {empr_id} and cotr1.cotr_id = {cotr_id} AND u1.unit_codigo = {unit_codigo}"""
          ).on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> "1.03"
            )
            .as(_parseMaterial *)
          _idx_inicial = _idx + 1
          _idx_final = _idx + 1
          _materiales.map {
            _m =>
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  _m._2 match {
                    case Some(value) => value
                    case None        => ""
                  },
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3 match {
                    case Some(value) => value
                    case None        => 0
                  },
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._4,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(G" + (_idx + 1) + "*H" + (_idx + 1) + ")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1
          }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (B)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          // MANO OBRA
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "CANT",
              "SALARIO",
              "RENDIMIENTO",
              "VALOR",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))            
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "MANO DE OBRA",
              "",
              "",
              "",
              "",
              "INTEGRAL / DIA",
              "UNID / DIA",
              "PARCIAL",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              // _listMerged01 += CellRange((_idx, _idx), (6, 7))
              // _listMerged01 += CellRange((_idx, _idx), (8, 9))
              // _listMerged01 += CellRange((_idx, _idx), (10, 11))
              // _listMerged01 += CellRange((_idx, _idx), (12, 13))            
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "(f)",
              "(g)",
              "(h)",
              "(f x g / h)",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
            _idx += 1            
          //
          // LEER MANO DE OBRA
          val _parseManoObra = str("maob_descripcion") ~ get[Option[Int]]("maobpr_precio") ~ double("maobpr_rendimiento") map { case a1 ~ a2 ~ a3 => (a1, a2, a3) }
          val _manoObra = SQL("""SELECT mob1.maob_descripcion, mop1.maobpr_precio, mop1.maobpr_rendimiento from siap.mano_obra mob1
                                 INNER JOIN siap.mano_obra_precio mop1 ON mop1.maob_id = mob1.maob_id
                                 WHERE mop1.maobpr_anho = {anho}""").
                                 on(
                                   'anho -> orden.cotr_anho.get
                                 ).as(_parseManoObra *)
          _idx_inicial = _idx + 1
          _idx_final = _idx + 1
          _manoObra.map { _m => 
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  1,
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2 match {
                    case Some(value) => value
                    case None        => 0
                  },
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(F" + (_idx + 1) + "* G" + (_idx + 1) + "/ H" + (_idx + 1) + ")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1            
          }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (C)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "SUBTOTAL SUMINISTRO Y MONTAJE",
                Some(6),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet               
              ),
              FormulaCell(
                "SUM(" + _listSubTotal.mkString(",") + ")",
                Some(9),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("#,##0")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
            _listMerged01 += CellRange((_idx, _idx), (1, 5))
            _listMerged01 += CellRange((_idx, _idx), (6, 8))
            _idx_final = _idx
            //_listMerged01 += CellRange((_idx, _idx), (12, 13))
            _idx += 1
          /// AQUI DEBE IR LA INFO DE INGENIERIA
          var _ingLista = new ArrayBuffer[(String, Double)]()
          _ingLista += (("INGENIERIA", 0.05))
          _ingLista += (("ADMINISTRACION DE LA OBRA", 0.28))
          _ingLista += (("INSPECTORES DE OBRA", 0.02))
          _idx_inicial = _idx + 1
          var _idx_subtotal = _idx
          _listSubTotal = new ArrayBuffer[String]()
          _listSubTotal += ("I"+ _idx_subtotal)
          _ingLista.map { _m =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2,
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("##0.00%")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),          
                FormulaCell(
                  "SUM(J" + (_idx_subtotal ) + "*I" + (_idx + 1) + ")",
                  Some(9),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 5))
              _listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _listSubTotal += ("J" + (_idx + 1))
              _idx += 1
              _idx_final += 1              
            }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "TOTAL PRECIO UNIDAD CONSTRUCTIVA",
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(" + _listSubTotal.mkString(",") + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listRow01 += emptyRow
          _listRow01 += emptyRow
          _idx = _idx + 3
    }
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_104(empresa: Empresa, orden: orden_trabajo_cobro) {
    db.withConnection { implicit connection =>
          val emptyRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("")

          val headerRow01 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", empresa.empr_descripcion)
          val headerRow02 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", empresa.muni_descripcion.get)
          val headerRow03 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("", "ANALISIS DE PRECIOS UNITARIOS")
          val headerRow04 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "",
              "Item",
              "1.04"
            )
          val headerRow05 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Obra",
              "",
              "MODERNIZACION A LED",
              "",
              "",
              "",
              "",
              "",
              "Und",
              "UND"
            )
          val headerRow06 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Area",
              "",
              orden.cotr_direccion.get,
              "",
              "",
              "",
              "",
              "",
              "Cant",
              orden.cotr_cantidad.get
            )
          val headerRow07 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "Actividad",
              "",
              "DESMONTE DE LUMINARIA" ,
              "",
              "",
              "",
              "",
              "",
              "",
              ""
            )
          val headerRow08 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "CANT",
              "TARIFA / DIA",
              "RENDIMIENTO",
              "VALOR PARCIAL",
              "",
              ""
            )
          val headerRow09 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "TRANSPORTE, EQUIPOS Y HERRAMIENTAS",
              "",
              "",
              "",
              "",
              "INST / EQUIPO",
              "UNID/DIA/EQU.",
              "",
              "",
              ""
            )
          val headerRow10 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "(a)",
              "(b)",
              "(c)",
              "a x b / c",
              "",
              ""
            )

          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow01
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow02
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow03
          _listMerged01 += CellRange((_idx, _idx), (1, 10))
          _idx += 1
          _listRow01 += headerRow04
          _idx += 1
          _listRow01 += headerRow05
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += headerRow06
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += headerRow07
          _listMerged01 += CellRange((_idx, _idx), (1, 2))
          _listMerged01 += CellRange((_idx, _idx), (3, 8))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += headerRow08
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += headerRow09
           _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += headerRow10
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          /// AQUI DEBE IR LA INFO DE TRANSPORTE, EQUIPO Y HERRAMIENTA
          var _herramientaLista = new ArrayBuffer[(String, Long, Long, Long)]()
          _herramientaLista += (("HERRAMIENTA MENOR", 2, 2647, 1))
          _herramientaLista += (("CAMIONETA", 1, 158908,8))
          var _idx_inicial = _idx + 1
          var _idx_final = _idx + 1          
          _herramientaLista.map { _m =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2,
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3,
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._4,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),                
                FormulaCell(
                  "SUM(F" + (_idx + 1) + "*G" + (_idx + 1) + "/H" + (_idx + 1) +")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1              
            }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (A)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "UND",
              "PRECIO POR",
              "CANTIDAD DE",
              "VALOR",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "MATERIALES",
              "",
              "",
              "",
              "",
              "UNIDAD",
              "MATERIAL/UND",
              "PARCIAL",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          val headerRow11 = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "",
              "(d)",
              "(e)",
              "d x e",
              "",
              ""
            )
          //_listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listRow01 += headerRow11
          _idx += 1
          val _parseMaterial = str("elem_descripcion") ~ get[Option[String]](
            "elpr_unidad"
          ) ~ get[Option[Int]]("elpr_precio") ~ double("cotrma_cantidad") map {
            case a1 ~ a2 ~ a3 ~ a4 => (a1, a2, a3, a4)
          }
          val _materiales = SQL(
            """select distinct e1.elem_descripcion, ep1.elpr_unidad, ep1.elpr_precio, cotm1.cotrma_cantidad from siap.cobro_orden_trabajo cotr1
                                 inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cotr1.cotr_id 
                                 inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                                 inner join siap.elemento_unitario eu1 on eu1.elem_id = e1.elem_id
                                 inner join siap.unitario u1 on u1.unit_id = eu1.unit_id
                                 left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cotr1.cotr_fecha)
                                 where cotr1.empr_id = {empr_id} and cotr1.cotr_id = {cotr_id} AND u1.unit_codigo = {unit_codigo}"""
          ).on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> "1.04"
            )
            .as(_parseMaterial *)
          _idx_inicial = _idx + 1
          _idx_final = _idx + 1
          _materiales.map {
            _m =>
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  _m._2 match {
                    case Some(value) => value
                    case None        => ""
                  },
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3 match {
                    case Some(value) => value
                    case None        => 0
                  },
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._4,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(G" + (_idx + 1) + "*H" + (_idx + 1) + ")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1
          }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (B)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          // MANO OBRA
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "CANT",
              "SALARIO",
              "RENDIMIENTO",
              "VALOR",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))            
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "MANO DE OBRA",
              "",
              "",
              "",
              "",
              "INTEGRAL / DIA",
              "UNID / DIA",
              "PARCIAL",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              // _listMerged01 += CellRange((_idx, _idx), (6, 7))
              // _listMerged01 += CellRange((_idx, _idx), (8, 9))
              // _listMerged01 += CellRange((_idx, _idx), (10, 11))
              // _listMerged01 += CellRange((_idx, _idx), (12, 13))            
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "",
              "",
              "",
              "",
              "",
              "(f)",
              "(g)",
              "(h)",
              "(f x g / h)",
              "",
              ""
            )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
            _idx += 1            
          //
          // LEER MANO DE OBRA
          val _parseManoObra = str("maob_descripcion") ~ get[Option[Int]]("maobpr_precio") ~ double("maobpr_rendimiento") map { case a1 ~ a2 ~ a3 => (a1, a2, a3) }
          val _manoObra = SQL("""SELECT mob1.maob_descripcion, mop1.maobpr_precio, mop1.maobpr_rendimiento from siap.mano_obra mob1
                                 INNER JOIN siap.mano_obra_precio mop1 ON mop1.maob_id = mob1.maob_id
                                 WHERE mop1.maobpr_anho = {anho}""").
                                 on(
                                   'anho -> orden.cotr_anho.get
                                 ).as(_parseManoObra *)
          _idx_inicial = _idx + 1
          _idx_final = _idx + 1
          _manoObra.map { _m => 
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  1,
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2 match {
                    case Some(value) => value
                    case None        => 0
                  },
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(F" + (_idx + 1) + "* G" + (_idx + 1) + "/ H" + (_idx + 1) + ")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 4))
              //_listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _idx += 1
              _idx_final += 1            
          }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "SUBTOTAL (C)",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            FormulaCell(
              "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          //_listMerged01 += CellRange((_idx, _idx), (12, 13))
          _listSubTotal += ("J" + (_idx + 1))
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "SUBTOTAL SUMINISTRO Y MONTAJE",
                Some(6),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet               
              ),
              FormulaCell(
                "SUM(" + _listSubTotal.mkString(",") + ")",
                Some(9),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("#,##0")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
            _listMerged01 += CellRange((_idx, _idx), (1, 5))
            _listMerged01 += CellRange((_idx, _idx), (6, 8))
            _idx_final = _idx
            //_listMerged01 += CellRange((_idx, _idx), (12, 13))
            _idx += 1
          /// AQUI DEBE IR LA INFO DE INGENIERIA
          var _ingLista = new ArrayBuffer[(String, Double)]()
          _ingLista += (("INGENIERIA", 0.05))
          _ingLista += (("ADMINISTRACION DE LA OBRA", 0.28))
          _ingLista += (("INSPECTORES DE OBRA", 0.02))
          _idx_inicial = _idx + 1
          var _idx_subtotal = _idx
          _listSubTotal = new ArrayBuffer[String]()
          _listSubTotal += ("I"+ _idx_subtotal)
          _ingLista.map { _m =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2,
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("##0.00%")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),          
                FormulaCell(
                  "SUM(J" + (_idx_subtotal ) + "*I" + (_idx + 1) + ")",
                  Some(9),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              _listMerged01 += CellRange((_idx, _idx), (1, 5))
              _listMerged01 += CellRange((_idx, _idx), (6, 7))
              //_listMerged01 += CellRange((_idx, _idx), (8, 9))
              //_listMerged01 += CellRange((_idx, _idx), (10, 11))
              //_listMerged01 += CellRange((_idx, _idx), (12, 13))
              _listSubTotal += ("J" + (_idx + 1))
              _idx += 1
              _idx_final += 1              
            }
          _idx_final -= 1
          // Subtotal B (Material)
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "TOTAL PRECIO UNIDAD CONSTRUCTIVA",
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(" + _listSubTotal.mkString(",") + ")",
              Some(9),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listRow01 += emptyRow
          _listRow01 += emptyRow
          _idx = _idx + 3
    }
  }


  def siap_orden_trabajo_cobro_modernizacion_hoja_4_reporte_luminaria(
      empresa: Empresa,
      orden: orden_trabajo_cobro
  ): Sheet = {
    db.withConnection { implicit connection =>
      var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
      var _listMerged01 = new ListBuffer[CellRange]()
      val _parseReporte = str("reti_descripcion") ~ int("repo_consecutivo") ~ int(
        "aap_id"
      ) map { case a1 ~ a2 ~ a3 => (a1, a2, a3) }
      val _reportes = SQL(
        """SELECT rt1.reti_descripcion, r1.repo_consecutivo, cotr1.aap_id FROM siap.cobro_orden_trabajo_reporte cotr1 
                               INNER JOIN siap.reporte r1 ON r1.repo_id = cotr1.repo_id
                               INNER JOIN siap.reporte_tipo rt1 ON rt1.reti_id = r1.reti_id
                               WHERE cotr1.cotr_id = {cotr_id}
                               ORDER BY rt1.reti_id, r1.repo_consecutivo"""
      ).on(
          'cotr_id -> orden.cotr_id
        )
        .as(_parseReporte *)
      Sheet(
        name = "Reporte_Luminaria",
        rows = {
          val emptyRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("")
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("Reporte Tipo", "Reporte No.", "Luminaria")
          _reportes.map {
            _r =>
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _r._1,
                  Some(0),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _r._2,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#####0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _r._3,
                  Some(2),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#####0")
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
          }
          _listRow01.toList
        }
      )
    }
  }
}
