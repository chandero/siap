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

import utilities.DataUtil

import org.checkerframework.checker.units.qual.s
import org.apache.poi.ss.usermodel.CellType

/*
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
    barr_id: Option[scala.Long],
    aaus_id: Option[scala.Long]
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
      get[Option[scala.Long]]("barr_id") ~
      get[Option[scala.Long]]("aaus_id") map {
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
            barr_id ~
            aaus_id =>
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
          barr_id,
          aaus_id
        )
    }
  }
}
*/
class Cobro6Repository @Inject()(
    dbapi: DBApi,
    usuarioService: UsuarioRepository,
    empresaService: EmpresaRepository,
    municipioService: MunicipioRepository,
    firmaService: FirmaRepository,
    barrioService: BarrioRepository,
    unitarioService: UnitarioRepository,
    generalService: GeneralRepository,
    n2l: N2T,
    dataUtil: DataUtil
)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")
  private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"
  
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

  def buscarPorEmpresaAnhoPeriodo(empr_id: Long, anho: Int, periodo: Int) = {
    db.withConnection { implicit connection =>

      var query = """SELECT cotr1.* FROM siap.cobro_orden_trabajo cotr1 
                     WHERE cotr1.empr_id = {empr_id} """
      if (anho > 0) { 
        query += "and cotr1.cotr_anho = {cotr_anho} and cotr1.cotr_periodo = {cotr_periodo} "
      }
      query += "order by cotr1.cotr_consecutivo"
      SQL(query)
        .on(
          'empr_id -> empr_id,
          'cotr_anho -> anho,
          'cotr_periodo -> periodo
        )
        .as(orden_trabajo_cobro._set *)
    }
  }  

  def siap_orden_trabajo_cobro_consecutivo(empr_id: Long): Int = {
    db.withConnection { implicit connection =>
      val query =
        "SELECT gene_numero FROM siap.general WHERE empr_id = {empr_id} and gene_id = 5"
      SQL(query)
        .on(
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[Int].single)
    }
  }  

  def siap_orden_trabajo_verificar(empr_id: Long, anho: Int, periodo: Int): Boolean = {
    db.withConnection { implicit connection =>
      val query =
        """SELECT CASE WHEN COUNT(*) > 0 THEN true ELSE false END FROM siap.cobro_orden_trabajo cotr1 
        WHERE cotr1.cotr_tipo_obra = {reti_id} AND cotr1.cotr_anho = {anho} AND cotr1.cotr_periodo = {periodo}  and empr_id = {empr_id}"""
      SQL(query)
        .on(
          'reti_id -> 6,
          'anho -> anho,
          'periodo -> periodo,
          'empr_id -> empr_id
        )
        .as(scalar[Boolean].single)
    }
  }

  def siap_obtener_orden_trabajo_cobro_modernizacion(
      empr_id: Long,
      filter: String,
      orderby: String
  ): Future[Iterable[orden_trabajo_cobro]] = Future {
    db.withConnection { implicit connection =>
      var query =
        """SELECT * FROM siap.cobro_orden_trabajo co1
           WHERE empr_id = {empr_id}
        """
      if (!filter.isEmpty) {
        query = query + " and " + filter
        // curr_page = 1
      }
      if (!orderby.isEmpty) {
        query = query + s" ORDER BY $orderby"
      } else {
        query = query + s" ORDER BY co1.cotr_anho, co1.cotr_periodo, co1.cotr_consecutivo"
      }           
      val _lista = SQL(query)
        .on(
          'empr_id -> empr_id
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
    val _hoy = Calendar.getInstance().getTime()
    var _listMaterialSinPrecio = new ListBuffer[(Integer, Integer, Integer, String, String)]()
    db.withTransaction { implicit connection =>
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
        str("cotr_tecnologia_nueva") ~ int("cotr_potencia_nueva") ~ get[Option[
        String
      ]](
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
                	amo1.aamo_descripcion as cotr_luminaria_anterior,
                	amo2.aamo_descripcion as cotr_luminaria_nueva,
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
                left join siap.aap_modelo amo1 on amo1.aamo_id = rdd1.aamo_id_anterior 
                left join siap.aap_modelo amo2 on amo2.aamo_id = rdd1.aamo_id
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
                  	max(amo1.aamo_descripcion) as cotr_luminaria_anterior,
                  	max(amo2.aamo_descripcion) as cotr_luminaria_nueva,
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
                left join siap.aap_modelo amo1 on amo1.aamo_id = rdd1.aamo_id_anterior 
                left join siap.aap_modelo amo2 on amo2.aamo_id = rdd1.aamo_id
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
                barr_id,
                cotr_fecha_proceso
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
                    {barr_id},
                    {cotr_fecha_proceso}
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
              'barr_id -> orden._1,
              'cotr_fecha_proceso -> _hoy
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
      var _parseOrdenMaterial = int("cotr_id") ~ int("cotr_tipo_obra") ~ str("cotr_tipo_obra_tipo") map {
        case a1 ~ a2 ~ a3 =>
          (a1, a2, a3)
      }
      var _orden = SQL("""SELECT cotr_id, cotr_tipo_obra, cotr_tipo_obra_tipo FROM siap.cobro_orden_trabajo WHERE cotr_anho = {anho} AND cotr_periodo = {mes} AND cotr_tipo_obra = {cotr_tipo_obra}""").on(
        'anho -> anho,
        'mes -> mes,
        'cotr_tipo_obra -> 6
      ).as(_parseOrdenMaterial *)
      val _parseMaterial = int("cotr_id") ~ int("elem_id") ~ get[Option[Double]](
        "elpr_precio"
      ) ~ double("even_cantidad") ~ get[Option[String]]("elpr_unidad") ~ get[Option[Int]]("aap_id") ~ str("elem_descripcion")  map {
        case a1 ~ a2 ~ a3 ~ a4 ~ a5 ~ a6 ~ a7 => (a1, a2, a3, a4, a5, a6, a7)
      }
      println("Lista Ordenes a cargar Material: " + _orden.map(x => x._1).mkString(","))
      // Procesar Material Por Cada Orden
      val _queryMaterial =
/*         """select
	          cot1.cotr_id, e1.elem_id, e1.elem_descripcion, ep1.elpr_unidad, ep1.elpr_precio, e1.elem_estado, re1.aap_id, sum(re1.even_cantidad_instalado) as even_cantidad
          from siap.cobro_orden_trabajo cot1
          inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
          inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id
          inner join siap.reporte_evento re1 on re1.repo_id = r1.repo_id and re1.aap_id = cotr1.aap_id AND re1.even_cantidad_instalado > 0 AND re1.even_estado < 8
          inner join siap.elemento e1 on e1.elem_id = re1.elem_id
          left join siap.unitario u1 on u1.unit_id = re1.unit_id
          left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cot1.cotr_fecha)
          where cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id}
          group by 
            	GROUPING SETS (
            		(1,2,3,4,5,6,7),
            		(1,2,3,4,5,6)
            	)
            order by 2""" */
           """
            select * from (select
	              cot1.cotr_id, e1.elem_id, e1.elem_descripcion, ep1.elpr_unidad, ep1.elpr_precio, e1.elem_estado, re1.aap_id, sum(re1.even_cantidad_instalado) as even_cantidad
              from siap.cobro_orden_trabajo cot1
              inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
              inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id
              inner join siap.reporte_evento re1 on re1.repo_id = r1.repo_id and re1.aap_id = cotr1.aap_id  AND re1.even_cantidad_instalado > 0 AND re1.even_estado < 8
              inner join siap.elemento e1 on e1.elem_id = re1.elem_id
              left join siap.unitario u1 on u1.unit_id = re1.unit_id
              left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cot1.cotr_fecha)
              where cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id}
              group by 
             		1,2,3,4,5,6,7
            union all 
            select
	              cot1.cotr_id, e1.elem_id, e1.elem_descripcion, ep1.elpr_unidad, ep1.elpr_precio, e1.elem_estado, NULL, sum(re1.even_cantidad_instalado) as even_cantidad
              from siap.cobro_orden_trabajo cot1
              inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
              inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id
              inner join siap.reporte_evento re1 on re1.repo_id = r1.repo_id and re1.aap_id = cotr1.aap_id  AND re1.even_cantidad_instalado > 0 AND re1.even_estado < 8
              inner join siap.elemento e1 on e1.elem_id = re1.elem_id
              left join siap.unitario u1 on u1.unit_id = re1.unit_id
              left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cot1.cotr_fecha)
              where cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id}
              group by 
            		  1,2,3,4,5,6
            ) as o
              order by o.elem_id, o.aap_id ASC"""            
      _orden.map { _o =>      
        println("Cargar Material Orden: " + _o._1)
        val _materiales = SQL(_queryMaterial)
          .on(
            'empr_id -> empr_id,
            'cotr_id -> _o._1
        )
        .as(_parseMaterial *)
        println("Insertando Material Orden: " + _o._1)
        val _queryInsertarMaterial =
          """INSERT INTO siap.cobro_orden_trabajo_material (cotr_id, elem_id, cotrma_valor_unitario, cotrma_cantidad, cotrma_unidad, aap_id) 
              VALUES (
                    {cotr_id}, 
                    {elem_id}, 
                    {cotrma_valor_unitario}, 
                    {cotrma_cantidad},
                    {cotrma_unidad},
                    {aap_id})"""
        val _queryActualizarMaterial = """UPDATE siap.cobro_orden_trabajo_material SET 
                        cotrma_cantidad_total = {cotrma_cantidad_total},
                        unit_id = (select unit_id from siap.elemento_unitario eu1 where eu1.elem_id = {elem_id} and eu1.elun_tipo_obra = {cotr_tipo_obra} and eu1.elun_tipo_obra_tipo = {cotr_tipo_obra_tipo} and {cotrma_cantidad_total} between eu1.elun_entre_min and eu1.elun_entre_max)
                        WHERE cotr_id = {cotr_id} AND elem_id = {elem_id}"""
        _materiales.map { material =>
          println("Insertando Material: " + material + ", cotr_id:" + _o._1)
          material._6 match {
            case Some(aap) =>
              SQL(_queryInsertarMaterial)
              .on(
                'cotr_id -> material._1,
                'elem_id -> material._2,
                'cotrma_valor_unitario -> material._3,
                'cotrma_cantidad -> material._4,
                'cotrma_unidad -> material._5,
                'aap_id -> material._6
              )
              .executeInsert()
          case None =>
            val _sql = SQL(_queryActualizarMaterial).on(
              'cotr_id -> material._1,
              'elem_id -> material._2,
              'cotrma_cantidad_total -> material._4,
              'cotr_tipo_obra -> _o._2,
              'cotr_tipo_obra_tipo -> _o._3
            )
            println("Actualizando Material: " + material + ", cotr_id:" + _o._1)
              _sql.executeUpdate()
            // verificar si el material tiene o no precio
            // Agregar material


          }
        }
      }

      //// ACTUALIZAR USO ORDEN DE TRABAJO
      SQL("""
              update siap.cobro_orden_trabajo co1 
              set aaus_id = o.aaus_id
              from
                (select cotr1.cotr_id, max(a1.aaus_id) as aaus_id from siap.cobro_orden_trabajo cot1
                  inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id
                  inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id 
                  inner join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id
                  inner join siap.aap a1 on a1.aap_id = rd1.aap_id
                  where rd1.even_estado < 8 and cot1.cotr_tipo_obra = 6 and cot1.cotr_anho = {anho} and cot1.cotr_periodo = {periodo} and cot1.empr_id = {empr_id}
                group by 1) as o
              where co1.cotr_id = o.cotr_id       
      """).on(
          'anho -> anho,
          'periodo -> mes,
          'empr_id -> empr_id
      ).executeUpdate()
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
      cotr_id: Long,
      usua_id: Long
  ): (Int, Array[Byte]) = {
    import Height._
    val empresa = empresaService.buscarPorId(empr_id)
    val orden = buscarPorId(empr_id, cotr_id)
    var barrio = barrioService.buscarPorId(orden.get.barr_id.get)
    var _listCuadroGeneralUnitario = new ListBuffer[
      (String, String, String, Double, String, String, String, String)
    ]()

    val sheet1 = siap_orden_trabajo_cobro_modernizacion_hoja_1_orden(
      empresa.get,
      orden.get
    )

    var sheet3 = siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios(
      empresa.get,
      orden.get,
      barrio.get,
      _listCuadroGeneralUnitario
    )

    val sheet2 = siap_orden_trabajo_cobro_modernizacion_hoja_2_cuadro_general(
      empresa.get,
      orden.get,
      barrio.get,
      _listCuadroGeneralUnitario
    )

    val sheet4 =
      siap_orden_trabajo_cobro_modernizacion_hoja_4_reporte_luminaria(
        empresa.get,
        orden.get
      )

    val sheet5 =
      siap_orden_trabajo_cobro_modernizacion_hoja_5_entrega(
        empresa.get,
        orden.get
      )

    val sheet98 =
      siap_orden_trabajo_cobro_modernizacion_hoja_98_resumen_material(
        empresa.get,
        orden.get,
        _listCuadroGeneralUnitario
      )

    val sheet99 = siap_orden_trabajo_cobro_modernizacion_hoja_99_generado(
      empresa.get,
      orden.get,
      usua_id
    )
    println("Escribiendo en el Stream")
    var os: ByteArrayOutputStream = new ByteArrayOutputStream()
    //set page setup to fit to one page width but multiple pages height
    val _sheet1Setup = PrintSetup(fitHeight = 1, fitWidth = 1)
    val _sheet2Setup = PrintSetup(fitHeight = 1, fitWidth = 1)
    val _sheet3Setup = PrintSetup(scale = 80)
    val _sheet5Setup = PrintSetup(fitHeight = 1, fitWidth = 1)
    sheet1.withPrintSetup(_sheet1Setup)
    sheet2.withPrintSetup(_sheet2Setup)
    sheet3.withPrintSetup(_sheet3Setup)
    sheet5.withPrintSetup(_sheet5Setup)    
    Workbook(sheet1, sheet2, sheet3, sheet4, sheet5, sheet98, sheet99)
      .writeToOutputStream(os)
    println("Stream Listo")
    (orden.get.cotr_consecutivo.get, os.toByteArray)
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_1_orden(
      empresa: Empresa,
      orden: orden_trabajo_cobro
  ): Sheet = {
    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged01 = new ListBuffer[com.norbitltd.spoiwo.model.CellRange]()
    val _fuenteNegrita = Font(
      height = 10.points,
      fontName = "Liberation Sans",
      bold = true,
      italic = false,
      strikeout = false
    )
    val _fuenteNormal = Font(
      height = 10.points,
      fontName = "Liberation Sans",
      bold = false,
      italic = false,
      strikeout = false
    )
    val _fechaConcesionStr =
      generalService.buscarPorId(6, empresa.empr_id.get).get.gene_valor.get
    val _fechaConcesion = DateTime
      .parse(_fechaConcesionStr, DateTimeFormat.forPattern("yyyy-MM-dd"))
      .toLocalDate()
    val _prefijo_interventoria = generalService.buscarPorId(10, empresa.empr_id.get).get.gene_valor.get
    var _idx = 0
    Sheet(
      name = "Orden",
      rows = {
        val emptyRow = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("")
        val header1 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "ORDEN DE TRABAJO No. " + _prefijo_interventoria + "-" + orden.cotr_consecutivo.get,
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(60.points)
        val header2 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "OBRA DE EXPANSION CONTRATO DE CONCESION No. " + empresa.empr_concesion.get,
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
        val header3 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "DEL " + _fechaConcesion
                .getDayOfMonth()
                .toString + " DE " + Utility
                .mes(_fechaConcesion.getMonthOfYear())
                .toUpperCase + " DE " + _fechaConcesion.getYear().toString,
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
        val header4 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "ALCALDIA MUNICIPAL DE " + empresa.muni_descripcion.get,
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
        val header5 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "INTERVENTOR ALUMBRADO PUBLICO",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )

        val rowFecha = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "",
            "FECHA",
            ":",
            Utility
              .mes(orden.cotr_fecha.get.getMonthOfYear())
              .toUpperCase() + " " + orden.cotr_fecha.get
              .getDayOfMonth()
              .toString + " DE " + orden.cotr_fecha.get.getYear().toString
          )
          .withHeight(20.points)

        val rowContratista = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "",
            "CONTRATISTA",
            ":",
            empresa.empr_descripcion
          )
          .withHeight(20.points)

        val rowNit = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "",
            "NIT",
            ":",
            empresa.empr_identificacion
          )
          .withHeight(20.points)

        val rowObjeto = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues(
            "",
            "OBJETO",
            ":",
            "OBRA COMPLEMENTARIA DE EXPANSION DEL ALUMBRADO PÚBLICO DEL MUNICIPIO DE " + empresa.muni_descripcion.get
          )
          .withHeight(30.points)

        val rowHeader = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "TIPO DE OPERACION",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "TECNOLOGIA",
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "POTENCIA(W)",
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
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
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "DESCRIPCION",
              Some(5),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "DIRECCION",
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(30.points)

        val contentRow = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "MODERNIZACION",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              orden.cotr_tecnologia_nueva.get,
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              orden.cotr_potencia_nueva.get,
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              orden.cotr_cantidad.get,
              Some(4),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "MODERNIZACION DE " + N2T
                .convertirLetras(orden.cotr_cantidad.get)
                .toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + (orden.cotr_luminaria_anterior match { case Some(v) => v case None => ""}) + " DE " + orden.cotr_potencia_anterior.get + "W" + " POR " + N2T
                .convertirLetras(orden.cotr_cantidad.get)
                .toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + orden.cotr_luminaria_nueva.get + " DE " + orden.cotr_potencia_nueva.get + " W",
              Some(5),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              orden.cotr_direccion.get,
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(40.points)

        // Textos Finales
        var _listaTextos = db.withConnection {
          implicit connection =>
            val _parse = int("cott_id") ~ int("cott_codigo") ~ str("cott_texto") map {
              case cott_id ~ cott_codigo ~ cott_texto =>
                (cott_id, cott_codigo, cott_texto)
            }
            SQL(
              """SELECT * FROM siap.cobro_orden_trabajo_texto WHERE empr_id = {empr_id}
              ORDER BY cott_codigo ASC"""
            ).on(
                'empr_id -> empresa.empr_id
              )
              .as(
                _parse *
              )
        }
        val _texto1 = _listaTextos(0)._3
        var _texto2 = _listaTextos(1)._3
        val _texto3 = _listaTextos(2)._3
        val _texto4 = _listaTextos(3)._3

        _texto2 = _texto2.replace("{contrato}", empresa.empr_concesion.get)
        _texto2 = _texto2.replace("{municipio}", empresa.muni_descripcion.get)

        val _rowTexto01 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Justify,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "PRECIO",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              ":",
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              _texto1,
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Justify,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(50.points)

        val _rowTexto02 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "FORMA DE PAGO",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              ":",
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              _texto2,
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Justify,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(40.points)

        val _rowTexto03 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "PLAZO DE EJECUCIÓN",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              ":",
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              _texto3,
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Justify,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(40.points)

        val _rowTexto04 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "ANEXOS",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              ":",
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              _texto4,
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Justify,
                  verticalAlignment = VA.Top,
                  wrapText = java.lang.Boolean.TRUE
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(40.points)

        var _firma01 = firmaService.buscarPorCodigo(3, empresa.empr_id.get)
        var _firma02 = firmaService.buscarPorCodigo(4, empresa.empr_id.get)
        var _rowFirmaNombre = com.norbitltd.spoiwo.model.Row(
          StringCell(
            _firma01.firm_nombre.get,
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            _firma02.firm_nombre.get,
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        val _rowFirmaTitulo = com.norbitltd.spoiwo.model.Row(
          StringCell(
            _firma01.firm_titulo.get,
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNormal
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            _firma02.firm_titulo.get,
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNormal
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        val _rowFirmaSubtitulo = com.norbitltd.spoiwo.model.Row(
          StringCell(
            "Interventor Contrato de Concesión No." + empresa.empr_concesion.get + " de " + _fechaConcesion
              .getYear()
              .toString,
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNormal
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )

        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1

        _listRow01 += header1
        _listMerged01 += CellRange((_idx, _idx), (1, 6))
        _idx = _idx + 1
        _listRow01 += header2
        _listMerged01 += CellRange((_idx, _idx), (1, 6))
        _idx = _idx + 1
        _listRow01 += header3
        _listMerged01 += CellRange((_idx, _idx), (1, 6))
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += header4
        _listMerged01 += CellRange((_idx, _idx), (1, 6))
        _idx = _idx + 1
        _listRow01 += header5
        _listMerged01 += CellRange((_idx, _idx), (1, 6))
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += rowFecha
        _idx = _idx + 1
        _listRow01 += rowContratista
        _idx = _idx + 1
        _listRow01 += rowNit
        _idx = _idx + 1
        _listRow01 += rowObjeto
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += rowHeader
        _idx = _idx + 1
        _listRow01 += contentRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += _rowTexto01
        _listMerged01 += CellRange((_idx, _idx), (3, 6))
        _idx = _idx + 1
        _listRow01 += _rowTexto02
        _listMerged01 += CellRange((_idx, _idx), (3, 6))
        _idx = _idx + 1
        _listRow01 += _rowTexto03
        _listMerged01 += CellRange((_idx, _idx), (3, 6))
        _idx = _idx + 1
        _listRow01 += _rowTexto04
        _listMerged01 += CellRange((_idx, _idx), (3, 6))
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += _rowFirmaNombre
        _listMerged01 += CellRange((_idx, _idx), (1, 3))
        _listMerged01 += CellRange((_idx, _idx), (5, 6))
        _idx = _idx + 1
        _listRow01 += _rowFirmaTitulo
        _listMerged01 += CellRange((_idx, _idx), (1, 3))
        _listMerged01 += CellRange((_idx, _idx), (5, 6))
        _idx = _idx + 1
        _listRow01 += _rowFirmaSubtitulo
        _listMerged01 += CellRange((_idx, _idx), (5, 6))

        _listRow01.toList
      },
      mergedRegions = _listMerged01.toList,
      columns = {
        var _listColumn = new ArrayBuffer[com.norbitltd.spoiwo.model.Column]()
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 0, width = new Width(6, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 1, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 2, width = new Width(11, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 3, width = new Width(8, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 4, width = new Width(8, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 5, width = new Width(30, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 6, width = new Width(30, WidthUnit.Character))
        _listColumn.toList
      }
    )
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_5_entrega(
      empresa: Empresa,
      orden: orden_trabajo_cobro
  ): Sheet = {
    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged01 = new ListBuffer[com.norbitltd.spoiwo.model.CellRange]()
    val _fuenteNegrita = Font(
      height = 10.points,
      fontName = "Liberation Sans",
      bold = true,
      italic = false,
      strikeout = false
    )
    val _fuenteNormal = Font(
      height = 10.points,
      fontName = "Liberation Sans",
      bold = false,
      italic = false,
      strikeout = false
    )
    val _fechaConcesionStr =
      generalService.buscarPorId(6, empresa.empr_id.get).get.gene_valor.get
    val _fechaConcesion = DateTime
      .parse(_fechaConcesionStr, DateTimeFormat.forPattern("yyyy-MM-dd"))
      .toLocalDate()
    val _prefijo_interventoria = generalService.buscarPorId(10, empresa.empr_id.get).get.gene_valor.get
    val _fechaSolucion = db.withConnection { implicit connection =>
      SQL("""select r1.repo_fechasolucion from siap.cobro_orden_trabajo_reporte cotr1
           inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id 
            where cotr1.cotr_id = {cotr_id} limit 1""")
        .on(
          'cotr_id -> orden.cotr_id.get
        )
        .as(SqlParser.scalar[DateTime].single)
    }

    val _luminarias = db.withConnection { implicit connection =>
      SQL(
        """select aap_id from siap.cobro_orden_trabajo_reporte cotr1
           where cotr1.cotr_id = {cotr_id}
        """
      ).on(
          'cotr_id -> orden.cotr_id.get
        )
        .as(SqlParser.scalar[Long].*)
    }
    val sdf = new java.text.SimpleDateFormat("yyyy-MM-dd")
    var _idx = 0
    Sheet(
      name = "Entrega",
      rows = {
        val emptyRow = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("")
        val header2 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "ORDEN DE TRABAJO No. " + _prefijo_interventoria + "-" + orden.cotr_consecutivo.get,
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
        val header4 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "MODERNIZACION A LED",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
        val header1 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "ALUMBRADO PUBLICO DE " + empresa.muni_descripcion.get,
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )

        val rowHeader = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "TIPO DE OPERACION",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "FECHA",
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "CODIGO LUMINARIAS",
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "POTENCIA(W)",
              Some(4),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "CANTIDAD",
              Some(5),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "DESCRIPCION",
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "DIRECCION",
              Some(7),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNegrita,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(30.points)

        val contentRow = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "MODERNIZACION",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            DateCell(
              _fechaSolucion.toDate(),
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("YYYY-MM-DD"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              _luminarias.mkString("-"),
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              orden.cotr_potencia_nueva.get,
              Some(4),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              orden.cotr_cantidad.get,
              Some(5),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "MODERNIZACION DE " + N2T
                .convertirLetras(orden.cotr_cantidad.get)
                .toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + (orden.cotr_luminaria_anterior match { case Some(v) => v case None => "" }) + " DE " + orden.cotr_potencia_anterior.get + "W" + " POR " + N2T
                .convertirLetras(orden.cotr_cantidad.get)
                .toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + orden.cotr_luminaria_nueva.get + " DE " + orden.cotr_potencia_nueva.get + " W",
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              orden.cotr_direccion.get,
              Some(7),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  font = _fuenteNormal,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(40.points)

        // Textos Finales
        var _rowEntrego = com.norbitltd.spoiwo.model.Row(
          StringCell(
            "ENTREGO",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "RECIBIO",
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )

        var _firma01 = firmaService.buscarPorCodigo(1, empresa.empr_id.get)
        var _firma02 = firmaService.buscarPorCodigo(2, empresa.empr_id.get)
        var _rowFirmaNombre = com.norbitltd.spoiwo.model.Row(
          StringCell(
            _firma01.firm_nombre.get,
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                font = _fuenteNegrita
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            _firma02.firm_nombre.get,
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                font = _fuenteNegrita
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        val _rowFirmaTitulo = com.norbitltd.spoiwo.model.Row(
          StringCell(
            _firma01.firm_titulo.get,
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                font = _fuenteNormal
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            _firma02.firm_titulo.get,
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                font = _fuenteNormal
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1

        _listRow01 += header1
        _listMerged01 += CellRange((_idx, _idx), (1, 7))
        _idx = _idx + 1
        _listRow01 += header2
        _listMerged01 += CellRange((_idx, _idx), (1, 7))
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += header4
        _listMerged01 += CellRange((_idx, _idx), (1, 7))
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += rowHeader
        _idx = _idx + 1
        _listRow01 += contentRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += _rowEntrego
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += emptyRow
        _idx = _idx + 1
        _listRow01 += _rowFirmaNombre
        _listMerged01 += CellRange((_idx, _idx), (1, 3))
        _listMerged01 += CellRange((_idx, _idx), (5, 7))
        _idx = _idx + 1
        _listRow01 += _rowFirmaTitulo
        _listMerged01 += CellRange((_idx, _idx), (1, 3))
        _listMerged01 += CellRange((_idx, _idx), (5, 7))
        _idx = _idx + 1
        _listRow01.toList
      },
      mergedRegions = _listMerged01.toList,
      columns = {
        var _listColumn = new ArrayBuffer[com.norbitltd.spoiwo.model.Column]()
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 0, width = new Width(2, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 1, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 2, width = new Width(11, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 3, width = new Width(30, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 4, width = new Width(8, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 5, width = new Width(12, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 6, width = new Width(30, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 7, width = new Width(30, WidthUnit.Character))
        _listColumn.toList
      }
    )
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_2_cuadro_general(
      empresa: Empresa,
      orden: orden_trabajo_cobro,
      barrio: Barrio,
      _listCuadroGeneralUnitario: ListBuffer[
        (String, String, String, Double, String, String, String, String)
      ]
  ): Sheet = {
    var _listRow02 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged02 = new ListBuffer[com.norbitltd.spoiwo.model.CellRange]()
    var _idx = 0
    val _prefijo_interventoria = generalService.buscarPorId(10, empresa.empr_id.get).get.gene_valor.get
    val _fuenteTitulo = Font(
      height = 10.points,
      fontName = "Liberation Sans",
      bold = true,
      italic = false,
      strikeout = false
    )
    val _fuenteTotal = Font(
      height = 10.points,
      fontName = "Liberation Sans",
      bold = true,
      italic = true,
      strikeout = false
    )
    val _fuenteSubTotal = Font(
      height = 9.points,
      fontName = "Liberation Sans",
      bold = true,
      italic = true,
      strikeout = false
    )
    val _fuenteCantidadTotal = Font(
      height = 9.points,
      fontName = "Liberation Sans",
      bold = true,
      italic = true,
      strikeout = false
    )
    val _fuenteNoBold = Font(
      height = 10.points,
      fontName = "Liberation Sans",
      bold = false,
      italic = false,
      strikeout = false
    )

    Sheet(
      name = "Cuadro General",
      rows = {
        val emptyRow = com.norbitltd.spoiwo.model
          .Row()
          .withCellValues("")
        val emptyFullRow = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                CellStyle(
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black
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
                  borders = CellBorders(
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
        val header1 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "ALUMBRADO PUBLICO " + empresa.muni_descripcion.get.toUpperCase(),
              Some(0),
              style = Some(
                CellStyle(
                  font = _fuenteTitulo,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
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
                  borders = CellBorders(
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
        val header2 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "ORDEN DE TRABAJO "+ _prefijo_interventoria + " No. " + "%06d".format(
                orden.cotr_consecutivo.get
              ) + " " + Utility
                .mes(orden.cotr_fecha.get.getMonthOfYear)
                .toUpperCase + "/" + orden.cotr_fecha.get.getYear,
              Some(0),
              style = Some(
                CellStyle(
                  font = _fuenteTitulo,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
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
                  borders = CellBorders(
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
        val header3 = com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "SECTOR" + " " + orden.cotr_direccion.get.toUpperCase(),
              Some(0),
              style = Some(
                CellStyle(
                  font = _fuenteTitulo,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(8),
              style = Some(
                CellStyle(
                  borders = CellBorders(
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
        val header4 = com.norbitltd.spoiwo.model.Row(
          StringCell(
            "CUADRO GENERAL",
            Some(0),
            style = Some(
              CellStyle(
                font = _fuenteTitulo,
                horizontalAlignment = HA.Center,
                verticalAlignment = VA.Center,
                borders = CellBorders(
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
            Some(8),
            style = Some(
              CellStyle(
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _listRow02 += emptyRow
        _idx = _idx + 1
        _listRow02 += header1
        _listMerged02 += CellRange((_idx, _idx), (0, 8))
        _idx = _idx + 1
        _listRow02 += header2
        _listMerged02 += CellRange((_idx, _idx), (0, 8))
        _idx = _idx + 1
        _listRow02 += header3
        _listMerged02 += CellRange((_idx, _idx), (0, 8))
        _idx = _idx + 1
        _listRow02 += header4
        _listMerged02 += CellRange((_idx, _idx), (0, 8))
        _idx = _idx + 1
        _listRow02 += com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "ITEM",
              index = Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "DESCRIPCION",
              index = Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "UND",
              index = Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "CANT",
              index = Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "MONTAJE DE EQUIPOS",
              index = Some(4),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "SUMINISTRO DE MATERIALES",
              index = Some(5),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "MONTAJE MANO DE OBRA",
              index = Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "COSTO UNITARIO PARCIAL",
              index = Some(7),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "COSTO PARCIAL ITEM",
              index = Some(8),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  font = _fuenteTitulo,
                  wrapText = true,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(50.points)
        _idx = _idx + 1
        var _idx_inicial = _idx + 1
        var _idx_final = _idx
        _listCuadroGeneralUnitario.map {
          _u =>
            _listRow02 += com.norbitltd.spoiwo.model
              .Row(
                StringCell(
                  _u._1,
                  index = Some(0),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@"),
                      horizontalAlignment = HA.Center,
                      verticalAlignment = VA.Center,
                      font = _fuenteNoBold,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thick,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  _u._2,
                  index = Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@"),
                      horizontalAlignment = HA.Left,
                      verticalAlignment = VA.Center,
                      font = _fuenteNoBold,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  _u._3,
                  index = Some(2),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@"),
                      horizontalAlignment = HA.Center,
                      verticalAlignment = VA.Center,
                      font = _fuenteNoBold,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _u._4,
                  index = Some(3),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0"),
                      horizontalAlignment = HA.Center,
                      verticalAlignment = VA.Center,
                      font = _fuenteNoBold,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  _u._5,
                  index = Some(4),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Right,
                      verticalAlignment = VA.Center,
                      dataFormat = CellDataFormat("#,##0"),
                      font = _fuenteNoBold,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  _u._6,
                  index = Some(5),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Right,
                      verticalAlignment = VA.Center,
                      dataFormat = CellDataFormat("#,##0"),
                      font = _fuenteNoBold,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  _u._7,
                  index = Some(6),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Right,
                      dataFormat = CellDataFormat("#,##0"),
                      verticalAlignment = VA.Center,
                      font = _fuenteNoBold,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  _u._8,
                  index = Some(7),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Right,
                      dataFormat = CellDataFormat("#,##0"),
                      verticalAlignment = VA.Center,
                      font = _fuenteNoBold,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(H" + (_idx + 1) + "*D" + (_idx + 1) + ")",
                  index = Some(8),
                  style = Some(
                    CellStyle(
                      horizontalAlignment = HA.Right,
                      verticalAlignment = VA.Center,
                      dataFormat = CellDataFormat("#,##0"),
                      font = _fuenteTotal,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thick,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              .withHeight(40.points)
            _idx = _idx + 1
            _idx_final = _idx_final + 1
        }
        _listRow02 += com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              index = Some(0),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VALOR COSTO DIRECTO",
              index = Some(1),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Right,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(2),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(3),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(4),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(5),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(6),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(7),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(I" + _idx_inicial + ":I" + _idx_final + ")",
              index = Some(8),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,#00"),
                  horizontalAlignment = HA.Right,
                  verticalAlignment = VA.Center,
                  font = _fuenteTitulo,
                  wrapText = true,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(40.points)
        // INGENIERIA
        val _ingParser = str("main_descripcion") ~ double("mainpr_precio") map {
          case main_descripcion ~ mainpr_precio =>
            (main_descripcion, mainpr_precio)
        }
        var _ingLista = db.withConnection {
          implicit connection =>
            SQL("""select main_descripcion, mainpr_precio from siap.mano_ingenieria mi1
                left join siap.mano_ingenieria_precio mip1 on mip1.main_id = mi1.main_id 
                where mip1.mainpr_anho = {anho} and mi1.empr_id = {empr_id}""")
              .on(
                'anho -> orden.cotr_anho.get,
                'empr_id -> empresa.empr_id
              )
              .as(_ingParser.*)
        }
        _idx = _idx + 1
        _idx_inicial = _idx
        var _idx_subtotal = _idx
        _idx_final = _idx + 1
        _ingLista.map {
          _m =>
            _listRow02 += com.norbitltd.spoiwo.model
              .Row(
                StringCell(
                  "",
                  Some(0),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@"),
                      horizontalAlignment = HA.Center,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thick,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  _m._1,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@"),
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
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
                      dataFormat = CellDataFormat("@"),
                      horizontalAlignment = HA.Center,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
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
                      dataFormat = CellDataFormat("@"),
                      horizontalAlignment = HA.Center,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
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
                      dataFormat = CellDataFormat("@"),
                      horizontalAlignment = HA.Center,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
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
                      dataFormat = CellDataFormat("@"),
                      horizontalAlignment = HA.Center,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
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
                      dataFormat = CellDataFormat("@"),
                      horizontalAlignment = HA.Center,
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._2,
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("##0.00%"),
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thin,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(I" + (_idx_subtotal) + "*H" + (_idx + 1) + ")",
                  Some(8),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0"),
                      borders = CellBorders(
                        leftStyle = CellBorderStyle.Thin,
                        leftColor = Color.Black,
                        topStyle = CellBorderStyle.Thin,
                        topColor = Color.Black,
                        bottomStyle = CellBorderStyle.Thin,
                        bottomColor = Color.Black,
                        rightStyle = CellBorderStyle.Thick,
                        rightColor = Color.Black
                      )
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
              .withHeight(30.points)
            _idx = _idx + 1
            _idx_final += 1
        }
        _idx_final = _idx_final - 1
        _listRow02 += com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              "",
              index = Some(0),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "TOTAL PROYECTO",
              index = Some(1),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Right,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(2),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(3),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(4),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(5),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(6),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(7),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  verticalAlignment = VA.Center,
                  wrapText = true,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(I" + _idx_inicial + ":I" + _idx_final + ")",
              index = Some(8),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  horizontalAlignment = HA.Right,
                  verticalAlignment = VA.Center,
                  font = _fuenteTitulo,
                  wrapText = true,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(40.points)
        // FIN INGENIERIA
        _listRow02 += emptyFullRow
        _listRow02 += emptyFullRow
        _listRow02 += emptyFullRow
        _listRow02 += emptyFullRow
        _listRow02 += emptyFullRow
        _idx = _idx + 6
        // FIRMAS CUADRO GENERAL
        // FIRMAS UNITARIOS
        var _firma03 = firmaService.buscarPorCodigo(3, empresa.empr_id.get)
        var _firma02 = firmaService.buscarPorCodigo(2, empresa.empr_id.get)
        _listRow02 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            _firma03.firm_nombre.get,
            Some(0),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteTitulo,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            _firma02.firm_nombre.get,
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteTitulo
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _listMerged02 += CellRange((_idx, _idx), (0, 1))
        _listMerged02 += CellRange((_idx, _idx), (5, 7))
        _idx = _idx + 1
        _listRow02 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            _firma03.firm_titulo.get,
            Some(0),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteTitulo,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            _firma02.firm_titulo.get,
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteTitulo
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _listMerged02 += CellRange((_idx, _idx), (0, 1))
        _listMerged02 += CellRange((_idx, _idx), (5, 7))
        _idx = _idx + 1
        _listRow02 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "CONCESIONARIO ALUMBRADO PUBLICO",
            Some(0),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteTitulo,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
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
                borders = CellBorders(
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
                borders = CellBorders(
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
                borders = CellBorders(
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
                borders = CellBorders(
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
                borders = CellBorders(
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
                borders = CellBorders(
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
                borders = CellBorders(
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
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
        _listMerged02 += CellRange((_idx, _idx), (0, 1))
        _idx = _idx + 1
        // FIN FIRMAS CUADRO GENERAL
        _listRow02.toList
      },
      mergedRegions = _listMerged02.toList,
      columns = {
        var _listColumn = new ArrayBuffer[com.norbitltd.spoiwo.model.Column]()
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 0, width = new Width(6, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 1, width = new Width(56, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 2, width = new Width(5, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 3, width = new Width(7, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 4, width = new Width(15, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 5, width = new Width(15, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 6, width = new Width(15, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 7, width = new Width(15, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 8, width = new Width(15, WidthUnit.Character))
        _listColumn.toList
      }
    )
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios(
      empresa: Empresa,
      orden: orden_trabajo_cobro,
      barrio: Barrio,
      _listCuadroGeneralUnitario: ListBuffer[
        (String, String, String, Double, String, String, String, String)
      ]
  ): Sheet = {
    db.withConnection { implicit connection =>
      val _fuenteTitulo = Font(
        height = 22.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
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
      var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
      var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
      var _listMerged01 = new ListBuffer[CellRange]()
      var _idx = 0

      var _unitarios = SQL(
        """select distinct u1.unit_codigo
                          from siap.cobro_orden_trabajo cot1
                          inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cot1.cotr_id
                          inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                          inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                          where cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and e1.elem_estado = 1
          union all
          select '1.04' as unit_codigo
          order by unit_codigo
          """
      ).on(
          'empr_id -> orden.empr_id,
          'cotr_id -> orden.cotr_id
        )
        .as(SqlParser.str("unit_codigo").*)
      val _elementParser = int("elem_id") ~ str("elem_descripcion") map { case elem_id ~ elem_descripcion => (elem_id, elem_descripcion) }
      _unitarios.map { unitario =>
        if (unitario == "1.01") {
          /// buscar si existe más de una ucap en el unitario para separar valores
          val elements =
            SQL("""select distinct e1.elem_id, e1.elem_descripcion
                          from siap.cobro_orden_trabajo cot1
                          inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cot1.cotr_id
                          inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                          inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                          where cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and u1.unit_codigo = {unitario} and e1.ucap_id = 1
                          order by e1.elem_id""")
              .on(
                'empr_id -> orden.empr_id,
                'cotr_id -> orden.cotr_id,
                'unitario -> "1.01"
              )
              .as(_elementParser *)
          println("Lista de Elementos de 1.01:" + elements)
          elements.map { e =>
            var _e: Option[(Int, String)] = Option.empty[(Int, String)]
            if (e._2.startsWith("LUMINARIA")) {
                _e = Some(e)
            }
            _idx = siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios(
              empresa,
              orden,
              barrio,
              unitario,
              _listRow01,
              _listMerged01,
              _idx,
              _listCuadroGeneralUnitario,
              _e
            )
          }
        } else {
          _idx = siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios(
            empresa,
            orden,
            barrio,
            unitario,
            _listRow01,
            _listMerged01,
            _idx,
            _listCuadroGeneralUnitario,
            None
          )
        }
      }
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
            .Column(index = 1, width = new Width(18, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 3, width = new Width(3, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 4, width = new Width(18, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 6, width = new Width(13, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 7, width = new Width(13, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 8, width = new Width(13, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 9, width = new Width(6, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 10, width = new Width(6, WidthUnit.Character))

          _listColumn.toList
        }
      )
    }
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios(
      empresa: Empresa,
      orden: orden_trabajo_cobro,
      barrio: Barrio,
      unit_codigo: String,
      _listRow01: ListBuffer[com.norbitltd.spoiwo.model.Row],
      _listMerged01: ListBuffer[CellRange],
      _idx01: Int,
      _listCuadroGeneralUnitario: ListBuffer[
        (String, String, String, Double, String, String, String, String)
      ],
      element: Option[(Int, String)]
  ): Int = {
    val elem_id = element match {
      case Some(e) => Some(e._1)
      case None => None
    }
    db.withConnection { implicit connection =>
      var _idx = _idx01
      val _unitario = unitarioService.buscarPorCodigo(unit_codigo).get
      var _unidad = unit_codigo match {
        case "1.01" => "UND"
        case "1.02" => "UND"
        case "1.03" => "ML"
        case "1.04" => "UND"
        case "1.05" => "UND"
        case "1.06" => "ML"
        case "1.07" => "UND"
        case "1.08" => "ML"
        case "1.09" => "UND"
        case "1.10" => "UND"
        case "1.11" => "UND"
        case "1.12" => "UND"
      }
      var _descripcion = unit_codigo match {
        case "1.01" => {
          "SUMINISTRO E INSTALACION DE " + { element match {
            case Some(e) => e._2
            case None => { 
                  "LUMINARIA " + orden.cotr_luminaria_nueva.get + " " + orden.cotr_tecnologia_nueva.get + " " + orden.cotr_potencia_nueva.get 
                       .toString() + "W"
                  }
            }
          }
        }
        case "1.02" | "1.03" | "1.08" | "1.09" | "1.10" | "1.11" | "1.12" =>
          "SUMINISTRO E INSTALACION DE " + _unitario.unit_descripcion.get
        case "1.04" => _unitario.unit_descripcion.get
        case "1.05" => "SUMINISTRO DE " + _unitario.unit_descripcion.get
        case "1.06" => _unitario.unit_descripcion.get
        case "1.07" => "INSTALACION " + _unitario.unit_descripcion.get
        case _      => _unitario.unit_descripcion.get
      }
      println("Descripcion: " + _descripcion)

      var _ucap_id = unit_codigo match {
        case "1.01" => 1
        case "1.02" => 2
        case "1.03" => 3
        case "1.04" => 4
        case "1.05" => 5
        case "1.06" => 6
        case "1.07" => 7
        case "1.08" => 8
        case "1.09" => 9
        case "1.10" => 10
        case "1.11" => 11
        case "1.12" => 12
        case _      => 0
      }
      var _cantidad_item = elem_id match {
        case Some(elem_id) =>
/*           val _qcant = """select SUM(even_cantidad_instalado) AS cantidad FROM
                siap.cobro_orden_trabajo cot1
                inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
                inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id
                inner join siap.reporte_evento re1 on re1.repo_id = r1.repo_id and re1.aap_id = cotr1.aap_id
                inner join siap.elemento e1 on e1.elem_id = re1.elem_id
                inner join siap.unitario u1 on u1.unit_id = re1.unit_id
                left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cot1.cotr_fecha)
                where cot1.empr_id = {empr_id} and cotr1.cotr_id = {cotr_id} AND u1.unit_codigo = {unit_codigo} AND e1.ucap_id = {ucap_id} AND e1.elem_id = {elem_id}""" */
          val _qcant = """SELECT SUM(cotrma_cantidad) FROM siap.cobro_orden_trabajo cot1
                        INNER JOIN siap.cobro_orden_trabajo_material cotrma1 ON cotrma1.cotr_id = cot1.cotr_id
                        WHERE cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and cotrma1.elem_id = {elem_id} AND cotrma1.unit_id = {unit_id}"""
          SQL(
            _qcant
          ).on(
              'empr_id -> empresa.empr_id,
              'cotr_id -> orden.cotr_id,
              // 'unit_codigo -> unit_codigo,
              //'ucap_id -> _ucap_id,
              'unit_id -> _ucap_id,
              'elem_id -> elem_id
            )
            .as(SqlParser.scalar[Double].singleOpt)

        case None =>
/*           val _qcant = """select SUM(even_cantidad_instalado) AS cantidad FROM
                siap.cobro_orden_trabajo cot1
                inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
                inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id
                inner join siap.reporte_evento re1 on re1.repo_id = r1.repo_id and re1.aap_id = cotr1.aap_id
                inner join siap.elemento e1 on e1.elem_id = re1.elem_id
                inner join siap.unitario u1 on u1.unit_id = re1.unit_id
                left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cot1.cotr_fecha)
                where cot1.empr_id = {empr_id} and cotr1.cotr_id = {cotr_id} AND u1.unit_codigo = {unit_codigo} AND e1.ucap_id = {ucap_id}"""
 */          val _qcant = """SELECT SUM(cotrma_cantidad) FROM siap.cobro_orden_trabajo cot1
                        INNER JOIN siap.cobro_orden_trabajo_material cotrma1 ON cotrma1.cotr_id = cot1.cotr_id
                        INNER JOIN siap.elemento e1 ON e1.elem_id = cotrma1.elem_id
                        WHERE cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and e1.ucap_id = {ucap_id} AND cotrma1.aap_id is not null"""
          SQL(
            _qcant
          ).on(
              'empr_id -> empresa.empr_id,
              'cotr_id -> orden.cotr_id,
              // 'unit_codigo -> unit_codigo,
              'ucap_id -> _ucap_id
            )
            .as(SqlParser.scalar[Double].singleOpt)

      }

      var _cantidad_xls = _cantidad_item match {
        case Some(v) => v
        case None    => orden.cotr_cantidad.get.toDouble
      }

      if (unit_codigo == "1.03") {
        _cantidad_xls = 1.0
      }

      var _listSubTotal = new ListBuffer[String]()
      var _listTotal = new ListBuffer[String]()

      val _fuenteTitulo = Font(
        height = 22.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteSubTitulo = Font(
        height = 12.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteNegrita = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteNormal = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = false,
        italic = false,
        strikeout = false
      )
      val _fuenteMoneda = Font(
        height = 9.points,
        fontName = "Liberation Sans",
        bold = false,
        italic = false,
        strikeout = false
      )
      val _fuenteSubTotalTitulo = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteSubTotalMoneda = Font(
        height = 11.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )

      val emptyRow = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      val emptyFullRow = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )

      val emptyNoneRow = com.norbitltd.spoiwo.model
        .Row()
        .withCellValues("")

      val headerRow01 =
        com.norbitltd.spoiwo.model
          .Row(
            StringCell(
              empresa.empr_descripcion,
              index = Some(1),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
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
              index = Some(2),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(3),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(4),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(5),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(6),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(7),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(8),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(9),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  font = _fuenteTitulo,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(10),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thick,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thick,
                    bottomColor = Color.Black,
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          .withHeight(22.points)
      val headerRow02 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "MUNICIPIO DE " + empresa.muni_descripcion.get,
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteTitulo,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      val headerRow03 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "ANALISIS DE PRECIOS UNITARIOS",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteSubTitulo,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteTitulo,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        .withHeight(12.points)
      val headerRow04 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "Item",
            index = Some(9),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            unit_codigo,
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )

      val headerRow05 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "Obra",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(2),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),          
          StringCell(
            "MODERNIZACION DE LUMINARIA",
            index = Some(3),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(4),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(5),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(6),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),                    
          StringCell(
            "",
            index = Some(7),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),          
          StringCell(
            "",
            index = Some(8),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),          
          StringCell(
            "Und",
            index = Some(9),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "UND",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )

      val headerRow06 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "Area",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            orden.cotr_direccion.get,
            index = Some(3),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "Cant",
            index = Some(9),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          NumericCell(
            _cantidad_xls, // orden.cotr_cantidad.get.toDouble,
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      var _cantidad = _cantidad_xls
      val headerRow07 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "Actividad",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(2),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),          
          StringCell(
            _descripcion,
            index = Some(3),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(4),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),          
          StringCell(
            "",
            index = Some(5),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),          
          StringCell(
            "",
            index = Some(6),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),        
          StringCell(
            "",
            index = Some(7),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),     
          StringCell(
            "",
            index = Some(8),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),                 
          StringCell(
            "",
            index = Some(9),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      val headerRow08 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "TRANSPORTE, EQUIPOS Y HERRAMIENTAS",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                verticalAlignment = VA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(2),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(3),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),    
          StringCell(
            "",
            index = Some(4),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),                          
          StringCell(
            "CANT",
            index = Some(5),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "TARIFA / DIA",
            index = Some(6),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "RENDIMIENTO",
            index = Some(7),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "VALOR PARCIAL",
            index = Some(8),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      val headerRow09 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(5),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "INST / EQUIPO",
            index = Some(6),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "UNID/DIA/EQU.",
            index = Some(7),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(8),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      val headerRow10 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(a)",
            index = Some(5),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(b)",
            index = Some(6),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(c)",
            index = Some(7),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(a x b / c)",
            index = Some(8),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Center,
                font = _fuenteNormal,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )

      _listRow01 += emptyNoneRow
      _idx += 1
      _listRow01 += headerRow01
      _listMerged01 += CellRange((_idx, _idx), (1, 10))
      _idx += 1
      _listRow01 += emptyFullRow
      _idx += 1
      _listRow01 += headerRow02
      _listMerged01 += CellRange((_idx, _idx), (1, 10))
      _idx += 1
      _listRow01 += emptyFullRow
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
      _listRow01 += emptyFullRow
      _idx += 1
      _listRow01 += headerRow08
      _listMerged01 += CellRange((_idx, _idx + 2), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += headerRow09
      // _listMerged01 += CellRange((_idx - 1, _idx), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += headerRow10
      //_listMerged01 += CellRange((_idx, _idx), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      var _idx_inicial = _idx + 1
      var _idx_final = _idx + 1       
      /// AQUI DEBE IR LA INFO DE TRANSPORTE, EQUIPO Y HERRAMIENTA
      _idx = siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_herramienta(
              empresa,
              orden,
              barrio,
              unit_codigo,
              _listRow01,
              _listMerged01,
              _idx,
              _listSubTotal,
              _listCuadroGeneralUnitario,
              elem_id
      )
      // DESDE AQUI TRANSPORTE Y HERRAMIENTA    
      /*
      val _herramientaParser = int("math_codigo") ~ str("math_descripcion") ~ double(
        "mathpr_precio"
      ) ~ bool("mathpr_es_porcentaje") ~ double("mathpr_cantidad") ~ double(
        "mathpr_rendimiento"
      ) map {
        case math_codigo ~ math_descripcion ~ mathpr_precio ~ mathpr_es_porcentaje ~ mathpr_cantidad ~ mathpr_rendimiento =>
          (
            math_codigo,
            math_descripcion,
            mathpr_cantidad,
            mathpr_precio,
            mathpr_rendimiento,
            mathpr_es_porcentaje
          )
      }
      var _herramientaLista = SQL(
        """select math_codigo, math_descripcion, mathpr_precio, mathpr_es_porcentaje, mathpr_cantidad, mathpr_rendimiento from siap.mano_transporte_herramienta mth1
              left join siap.mano_transporte_herramienta_precio mthp1 on mthp1.math_id = mth1.math_id 
              where mthp1.mathpr_anho = {anho} and mth1.empr_id = {empr_id}
              order by mth1.math_id ASC"""
      ).on(
          'anho -> orden.cotr_anho.get,
          'empr_id -> empresa.empr_id
        )
        .as(_herramientaParser.*)

       _herramientaLista.map { _m =>
        if ((_m._1 == 2 && (orden.aaus_id.get == 3 || orden.aaus_id.get == 4)) || (_m._1 == 3 && (orden.aaus_id.get != 3 && orden.aaus_id.get != 4))) {
          None
        } else {
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              _m._2,
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(2),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  font = _fuenteNegrita,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),            
            NumericCell(
              _m._3,
              Some(5),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _m._4,
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0.00"),
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _m._5,
              Some(7),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0.00"),
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(F" + (_idx + 1) + "*G" + (_idx + 1) + "/H" + (_idx + 1) + ")",
              Some(8),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(10),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  borders = CellBorders(
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          _listMerged01 += CellRange((_idx, _idx), (9, 10))
          _idx += 1
          _idx_final += 1
        }
      }
      _idx_final -= 1
      // Subtotal B (Material)
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "SUBTOTAL (A)",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              font = _fuenteSubTotalTitulo,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              font = _fuenteSubTotalTitulo,
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        FormulaCell(
          "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("#,##0"),
              font = _fuenteSubTotalMoneda,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      */
      // HASTA AQUI TRANSPORTE HERRAMIENTAS
      var _subtotal_a = "+Unitarios!$J$" + (_idx + 1);
      _listMerged01 += CellRange((_idx, _idx), (1, 4))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _listSubTotal += ("J" + (_idx + 1))
      _idx += 1
      _listRow01 += emptyFullRow
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "MATERIALES",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                verticalAlignment = VA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "UND",
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "PRECIO POR",
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "CANTIDAD DE",
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      _listMerged01 += CellRange((_idx, _idx + 2), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "UNIDAD",
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "MATERIAL/UND",
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "PARCIAL",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      //_listMerged01 += CellRange((_idx, _idx), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      val headerRow11 = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(d)",
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(e)",
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(d x e)",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      //_listMerged01 += CellRange((_idx, _idx), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _listRow01 += headerRow11
      _idx += 1
      val _parseMaterial = str("elem_descripcion") ~ get[Option[String]](
        "elpr_unidad"
      ) ~ get[Option[Int]]("elpr_precio") ~ double("even_cantidad") map {
        case a1 ~ a2 ~ a3 ~ a4 => (a1, a2, a3, a4)
      }
      val _materiales = elem_id match {
        case Some(elem_id) =>
          // Buscar Luminarias Correspondientes para el material correcto
          var _luminarias = SQL(
            """select cotm1.aap_id
                          from siap.cobro_orden_trabajo cot1
                          inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cot1.cotr_id 
                          inner join siap.elemento e1 ON e1.elem_id = cotm1.elem_id
                          inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                          where cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and u1.unit_codigo = {unit_codigo} and e1.ucap_id = 1 and e1.elem_id = {elem_id}
                          order by cotm1.aap_id"""
          ).on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> unit_codigo,
              'elem_id -> elem_id
            )
            .as(
              SqlParser.scalar[Int] *
            )
          println("Luminarias: (" + _luminarias.mkString(",") + ")")
          /* val _qmat = """select e1.elem_descripcion, ep1.elpr_unidad, ep1.elpr_precio, SUM(re1.even_cantidad_instalado) as cotrma_cantidad
                      from siap.cobro_orden_trabajo cot1
                      inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
                      inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id
                      inner join siap.reporte_evento re1 on re1.repo_id = r1.repo_id and re1.aap_id = cotr1.aap_id
                      inner join siap.elemento e1 on e1.elem_id = re1.elem_id and e1.elem_estado = 1
                      inner join siap.unitario u1 on u1.unit_id = re1.unit_id
                      left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cot1.cotr_fecha)
                      where cot1.empr_id = {empr_id} and cotr1.cotr_id = {cotr_id} AND u1.unit_codigo = {unit_codigo} and CAST(re1.aap_id as VARCHAR) IN ({luminarias})
                      group by 1,2,3""" */
          val _qmat = """select e1.elem_descripcion, cotm1.cotrma_unidad AS elpr_unidad, cotm1.cotrma_valor_unitario AS elpr_precio, SUM(cotm1.cotrma_cantidad) as even_cantidad FROM siap.cobro_orden_trabajo cot1
                         inner join siap.cobro_orden_trabajo_material cotm1 ON cotm1.cotr_id = cot1.cotr_id
                         inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                         inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                         where cot1.empr_id = {empr_id} AND cot1.cotr_id = {cotr_id} and u1.unit_codigo = {unit_codigo} AND e1.elem_estado = 1 AND CAST(cotm1.aap_id as VARCHAR) IN ({luminarias})
                         group by 1,2,3"""
          SQL(_qmat)
            .on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> unit_codigo,
              'luminarias -> _luminarias.mkString(",").split(",").toSeq
            )
            .as(_parseMaterial *)
        case None =>
          /* val _qmat ="""select e1.elem_descripcion, ep1.elpr_unidad, ep1.elpr_precio, SUM(re1.even_cantidad_instalado) as cotrma_cantidad
                from siap.cobro_orden_trabajo cot1
                inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
                inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id
                inner join siap.reporte_evento re1 on re1.repo_id = r1.repo_id and re1.aap_id = cotr1.aap_id
                inner join siap.elemento e1 on e1.elem_id = re1.elem_id and e1.elem_estado = 1
                inner join siap.unitario u1 on u1.unit_id = re1.unit_id
                left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cot1.cotr_fecha)
              where cot1.empr_id = {empr_id} and cotr1.cotr_id = {cotr_id} AND u1.unit_codigo = {unit_codigo}
              group by 1,2,3""" */
          val _qmat = """select e1.elem_descripcion, cotm1.cotrma_unidad AS elpr_unidad, cotm1.cotrma_valor_unitario AS elpr_precio, SUM(cotm1.cotrma_cantidad) AS even_cantidad from siap.cobro_orden_trabajo_material cotm1
                         inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                         inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                         where cotm1.cotr_id = {cotr_id} and u1.unit_codigo = {unit_codigo} AND e1.elem_estado = 1
                         group by 1,2,3"""
          SQL(
            _qmat
          ).on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> unit_codigo
            )
            .as(_parseMaterial *)
      }
      _idx_inicial = _idx + 1
      _idx_final = _idx + 1
      _materiales.map { _m =>
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            _m._1,
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,                  
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                )
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
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
                dataFormat = CellDataFormat("#,##0"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          NumericCell(
/*             if (_m._1.startsWith("FOTOCELDA")) {
              if (_m._4 == _cantidad_xls){
                _m._4 / _cantidad_xls
              } else if ( _m._4 == 1) {
                _m._4
              } else {
                1.00
              }
            } else {
              _m._4 / _cantidad_xls,
            } */
            _m._4 / _cantidad_xls,
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("#,##0.00"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(G" + (_idx + 1) + "*H" + (_idx + 1) + ")",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("#,##0"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _listMerged01 += CellRange((_idx, _idx), (1, 4))
        //_listMerged01 += CellRange((_idx, _idx), (6, 7))
        //_listMerged01 += CellRange((_idx, _idx), (8, 9))
        //_listMerged01 += CellRange((_idx, _idx), (10, 11))
        _listMerged01 += CellRange((_idx, _idx), (9, 10))
        _idx += 1
        _idx_final += 1
      }
      _idx_final -= 1
      // Subtotal B (Material)
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "SUBTOTAL (B)",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              font = _fuenteSubTotalTitulo,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        FormulaCell(
          "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("#,##0"),
              font = _fuenteSubTotalMoneda,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
      var _subtotal_b = "+Unitarios!$J$" + (_idx + 1);
      _listMerged01 += CellRange((_idx, _idx), (1, 4))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _listSubTotal += ("J" + (_idx + 1))
      _idx += 1
      // MANO OBRA
      _idx = siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_mano_obra(
              empresa,
              orden,
              barrio,
              unit_codigo,
              _listRow01,
              _listMerged01,
              _idx,
              _listSubTotal,
              _listCuadroGeneralUnitario,
              elem_id
      )
      // DESDE AQUI MANO DE OBRA
      /*
      _listRow01 += emptyFullRow
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "MANO DE OBRA",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                verticalAlignment = VA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "CANT",
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "SALARIO",
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "RENDIMIENTO",
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      _listMerged01 += CellRange((_idx, _idx + 2), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "INTEGRAL / DIA",
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "UNID / DIA",
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "PARCIAL",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      // _listMerged01 += CellRange((_idx, _idx), (1, 4))
      // _listMerged01 += CellRange((_idx, _idx), (6, 7))
      // _listMerged01 += CellRange((_idx, _idx), (8, 9))
      // _listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(f)",
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(g)",
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(f x g / h)",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      //_listMerged01 += CellRange((_idx, _idx), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      //
      // LEER MANO DE OBRA
      val _parseManoObra = str("maob_descripcion") ~ get[Option[Int]](
        "maobpr_precio"
      ) ~ double("maobpr_rendimiento") map { case a1 ~ a2 ~ a3 => (a1, a2, a3) }
      val _manoObra = SQL(
        """SELECT mob1.maob_descripcion, mop1.maobpr_precio, mop1.maobpr_rendimiento from siap.mano_obra mob1
                                 INNER JOIN siap.mano_obra_precio mop1 ON mop1.maob_id = mob1.maob_id
                                 WHERE mop1.maobpr_anho = {anho} and mob1.empr_id = {empr_id}"""
      ).on(
          'anho -> orden.cotr_anho.get,
          'empr_id -> empresa.empr_id
        )
        .as(_parseManoObra *)
      _idx_inicial = _idx + 1
      _idx_final = _idx + 1
      _manoObra.map { _m =>
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            _m._1,
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),               
          NumericCell(
            1,
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
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
                dataFormat = CellDataFormat("#,##0"),
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          NumericCell(
            _m._3,
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("#,##0.00"),
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(F" + (_idx + 1) + "* G" + (_idx + 1) + "/ H" + (_idx + 1) + ")",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("#,##0"),
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _listMerged01 += CellRange((_idx, _idx), (1, 4))
        //_listMerged01 += CellRange((_idx, _idx), (6, 7))
        //_listMerged01 += CellRange((_idx, _idx), (8, 9))
        //_listMerged01 += CellRange((_idx, _idx), (10, 11))
        _listMerged01 += CellRange((_idx, _idx), (9, 10))
        _idx += 1
        _idx_final += 1
      }
      _idx_final -= 1
      // Subtotal C (Mano Obra)
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "SUBTOTAL (C)",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              font = _fuenteSubTotalTitulo,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        FormulaCell(
          "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("#,##0"),
              font = _fuenteSubTotalMoneda,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
      */
      // HASTA AQUI MANO DE OBRA SUBTOTAL C
      var _subtotal_c = "+Unitarios!$J$" + (_idx + 1);
      _listMerged01 += CellRange((_idx, _idx), (1, 4))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _listSubTotal += ("J" + (_idx + 1))
      _idx += 1
      _listRow01 += emptyFullRow
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),                 
        StringCell(
          "SUBTOTAL SUMINISTRO Y MONTAJE",
          Some(6),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              font = _fuenteSubTotalTitulo,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thin,
                rightColor = Color.Black,
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),             
        FormulaCell(
          "SUM(" + _listSubTotal.mkString(",") + ")",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("#,##0"),
              font = _fuenteSubTotalMoneda,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thin,
                rightColor = Color.Black,
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                topStyle = CellBorderStyle.Thick,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      var _subtotal_suministro_montaje = "+Unitarios!$J$" + (_idx + 1);
      _listMerged01 += CellRange((_idx, _idx), (1, 5))
      _listMerged01 += CellRange((_idx, _idx), (6, 8))
      _idx_final = _idx
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),                
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (1, 5))
      _listMerged01 += CellRange((_idx, _idx), (6, 8))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      var _idx_subtotal = _idx
      _idx = _idx + 1

      _listCuadroGeneralUnitario += (
        (
          unit_codigo,
          _descripcion,
          _unidad,
          _cantidad,
          _subtotal_a,
          _subtotal_b,
          _subtotal_c,
          _subtotal_suministro_montaje
        )
      )
      /// AQUI DEBE IR LA INFO DE INGENIERIA
      val _ingParser = str("main_descripcion") ~ double("mainpr_precio") map {
        case main_descripcion ~ mainpr_precio =>
          (main_descripcion, mainpr_precio)
      }
      var _ingLista = SQL(
        """select main_descripcion, mainpr_precio from siap.mano_ingenieria mi1
                left join siap.mano_ingenieria_precio mip1 on mip1.main_id = mi1.main_id 
                where mip1.mainpr_anho = {anho} and mi1.empr_id = {empr_id} ORDER BY mi1.main_id ASC"""
      ).on(
          'anho -> orden.cotr_anho.get,
          'empr_id -> empresa.empr_id
        )
        .as(_ingParser.*)
      _idx_inicial = _idx + 1
      _listSubTotal = new ListBuffer[String]()
      _listSubTotal += ("J" + _idx_subtotal)
      _ingLista.map { _m =>
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            _m._1,
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),          
          NumericCell(
            _m._2,
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("##0.00%"),
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(J" + (_idx_subtotal) + "*I" + (_idx + 1) + ")",
            Some(9),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("#,##0"),
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _listMerged01 += CellRange((_idx, _idx), (1, 5))
        _listMerged01 += CellRange((_idx, _idx), (6, 7))
        _listMerged01 += CellRange((_idx, _idx), (9, 10))
        _listSubTotal += ("J" + (_idx + 1))
        _idx += 1
        _idx_final += 1
      }
      _idx_final -= 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ), 
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (1, 5))
      _listMerged01 += CellRange((_idx, _idx), (6, 8))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx = _idx + 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (1, 5))
      _listMerged01 += CellRange((_idx, _idx), (6, 8))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx = _idx + 1
      // FIRMAS UNITARIOS
      var _firma01 = firmaService.buscarPorCodigo(1, empresa.empr_id.get)
      var _firma02 = firmaService.buscarPorCodigo(2, empresa.empr_id.get)
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          _firma01.firm_nombre.get,
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          _firma02.firm_nombre.get,
          Some(4),
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
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (1, 2))
      _listMerged01 += CellRange((_idx, _idx), (4, 5))
      _listMerged01 += CellRange((_idx, _idx), (6, 8))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx = _idx + 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          _firma01.firm_titulo.get,
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          _firma02.firm_titulo.get,
          Some(4),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
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
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),                
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (1, 2))
      _listMerged01 += CellRange((_idx, _idx), (4, 5))
      _listMerged01 += CellRange((_idx, _idx), (6, 8))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx = _idx + 1
      // TOTAL UCAP
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),        
        StringCell(
          "TOTAL PRECIO UNIDAD CONSTRUCTIVA",
          Some(6),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              font = _fuenteSubTotalTitulo,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
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
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),              
        FormulaCell(
          "SUM(" + _listSubTotal.mkString(",") + ")",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("#,##0"),
              font = _fuenteSubTotalMoneda,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
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
      _listMerged01 += CellRange((_idx, _idx), (1, 5))
      _listMerged01 += CellRange((_idx, _idx), (6, 8))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx = _idx + 1
      _idx
    }
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_herramienta(
      empresa: Empresa,
      orden: orden_trabajo_cobro,
      barrio: Barrio,
      unit_codigo: String,
      _listRow01: ListBuffer[com.norbitltd.spoiwo.model.Row],
      _listMerged01: ListBuffer[CellRange],
      _idx01: Int,
      _listSubTotal: ListBuffer[String],
      _listCuadroGeneralUnitario: ListBuffer[
        (String, String, String, Double, String, String, String, String)
      ],
      elem_id: Option[Int]
  ): Int = {
      var _idx = _idx01
      val _fuenteTitulo = Font(
        height = 22.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteSubTitulo = Font(
        height = 12.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteNegrita = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteNormal = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = false,
        italic = false,
        strikeout = false
      )
      val _fuenteMoneda = Font(
        height = 9.points,
        fontName = "Liberation Sans",
        bold = false,
        italic = false,
        strikeout = false
      )
      val _fuenteSubTotalTitulo = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteSubTotalMoneda = Font(
        height = 11.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )

      val emptyRow = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      val emptyFullRow = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )

      val emptyNoneRow = com.norbitltd.spoiwo.model
        .Row()
        .withCellValues("")

      val _herramientaParser = int("math_codigo") ~ str("math_descripcion") ~ double(
        "mathpr_precio"
      ) ~ bool("mathpr_es_porcentaje") ~ double("mathpr_cantidad") ~ double(
        "mathpr_rendimiento"
      ) map {
        case math_codigo ~ math_descripcion ~ mathpr_precio ~ mathpr_es_porcentaje ~ mathpr_cantidad ~ mathpr_rendimiento =>
          (
            math_codigo,
            math_descripcion,
            mathpr_cantidad,
            mathpr_precio,
            mathpr_rendimiento,
            mathpr_es_porcentaje
          )
      }
      var _herramientaLista = db.withConnection { implicit connection =>
        SQL(
        """select mth1.math_codigo, mth1.math_descripcion, mthp1.mathpr_precio, mthp1.mathpr_es_porcentaje, mthp1.mathpr_cantidad, mthp1.mathpr_rendimiento from siap.mano_transporte_herramienta mth1
              left join siap.mano_transporte_herramienta_precio mthp1 on mthp1.math_id = mth1.math_id 
              where mthp1.mathpr_anho = {anho} and mth1.empr_id = {empr_id} and
              mthp1.cotr_tipo_obra = {cotr_tipo_obra} and mthp1.cotr_tipo_obra_tipo = {cotr_tipo_obra_tipo}
              order by mth1.math_id ASC"""
      ).on(
          'anho -> orden.cotr_anho.get,
          'empr_id -> empresa.empr_id,
          'cotr_tipo_obra -> orden.cotr_tipo_obra.get,
          'cotr_tipo_obra_tipo -> orden.cotr_tipo_obra_tipo.get
        )
        .as(_herramientaParser.*)
      }
      var _idx_inicial = _idx + 1
      var _idx_final = _idx + 1
      _herramientaLista.map { _m =>
/*         if ((_m._1 == 2 && (orden.aaus_id.get == 3 || orden.aaus_id.get == 4)) || (_m._1 == 3 && (orden.aaus_id.get != 3 && orden.aaus_id.get != 4)) || (orden.cotr_tipo_obra_tipo.get.trim == '3')) {
          None
        } else */
        if (
            (_m._1 != 2 && _m._1 != 3) ||
            (_m._1 == 3 && orden.cotr_tipo_obra.get == 2 && orden.cotr_tipo_obra_tipo.get.trim.toInt == 3) ||
            (_m._1 == 2 && (orden.aaus_id.get != 3 && orden.aaus_id.get != 4) && (!(orden.cotr_tipo_obra.get == 2 && orden.cotr_tipo_obra_tipo.get.trim.toInt == 3))) ||
            (_m._1 == 3 && (orden.aaus_id.get == 3 || orden.aaus_id.get == 4))           
        ) {
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              _m._2,
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thick,
                    leftColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              index = Some(2),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  font = _fuenteNegrita,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),        
            StringCell(
              "",
              index = Some(3),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  font = _fuenteNegrita,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),    
            StringCell(
              "",
              index = Some(4),
              style = Some(
                CellStyle(
                  horizontalAlignment = HA.Left,
                  font = _fuenteNegrita,
                  borders = CellBorders(
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),         
            NumericCell(
              _m._3,
              Some(5),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  horizontalAlignment = HA.Center,
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _m._4,
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0.00"),
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _m._5,
              Some(7),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0.00"),
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(F" + (_idx + 1) + "*G" + (_idx + 1) + "/H" + (_idx + 1) + ")",
              Some(8),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  borders = CellBorders(
                    leftStyle = CellBorderStyle.Thin,
                    leftColor = Color.Black,
                    rightStyle = CellBorderStyle.Thin,
                    rightColor = Color.Black,
                    topStyle = CellBorderStyle.Thin,
                    topColor = Color.Black,
                    bottomStyle = CellBorderStyle.Thin,
                    bottomColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "",
              Some(10),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0"),
                  borders = CellBorders(
                    rightStyle = CellBorderStyle.Thick,
                    rightColor = Color.Black
                  )
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listMerged01 += CellRange((_idx, _idx), (1, 4))
          //_listMerged01 += CellRange((_idx, _idx), (6, 7))
          //_listMerged01 += CellRange((_idx, _idx), (8, 9))
          //_listMerged01 += CellRange((_idx, _idx), (10, 11))
          _listMerged01 += CellRange((_idx, _idx), (9, 10))
          _idx += 1
          _idx_final += 1
        } else {
          None
        }
      }
      _idx_final -= 1
      // Subtotal B (Material)
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "SUBTOTAL (A)",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              font = _fuenteSubTotalTitulo,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              font = _fuenteSubTotalTitulo,
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        FormulaCell(
          "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("#,##0"),
              font = _fuenteSubTotalMoneda,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
      _idx
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_mano_obra(
      empresa: Empresa,
      orden: orden_trabajo_cobro,
      barrio: Barrio,
      unit_codigo: String,
      _listRow01: ListBuffer[com.norbitltd.spoiwo.model.Row],
      _listMerged01: ListBuffer[CellRange],
      _idx01: Int,
      _listSubTotal: ListBuffer[String],
      _listCuadroGeneralUnitario: ListBuffer[
        (String, String, String, Double, String, String, String, String)
      ],
      elem_id: Option[Int]
  ): Int = {
      var _idx = _idx01

      val _fuenteTitulo = Font(
        height = 22.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteSubTitulo = Font(
        height = 12.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteNegrita = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteNormal = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = false,
        italic = false,
        strikeout = false
      )
      val _fuenteMoneda = Font(
        height = 9.points,
        fontName = "Liberation Sans",
        bold = false,
        italic = false,
        strikeout = false
      )
      val _fuenteSubTotalTitulo = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _fuenteSubTotalMoneda = Font(
        height = 11.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )

      val emptyRow = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      val emptyFullRow = com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            index = Some(1),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            index = Some(10),
            style = Some(
              CellStyle(
                horizontalAlignment = HA.Left,
                font = _fuenteNormal,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )

      val emptyNoneRow = com.norbitltd.spoiwo.model
        .Row()
        .withCellValues("")      

      _listRow01 += emptyFullRow
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "MANO DE OBRA",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                verticalAlignment = VA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "CANT",
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "SALARIO",
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "RENDIMIENTO",
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      _listMerged01 += CellRange((_idx, _idx + 2), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "INTEGRAL / DIA",
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "UNID / DIA",
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "PARCIAL",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      // _listMerged01 += CellRange((_idx, _idx), (1, 4))
      // _listMerged01 += CellRange((_idx, _idx), (6, 7))
      // _listMerged01 += CellRange((_idx, _idx), (8, 9))
      // _listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model
        .Row(
          StringCell(
            "",
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black
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
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(f)",
            Some(6),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(g)",
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "(f x g / h)",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black,
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                font = _fuenteNegrita,
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
      //_listMerged01 += CellRange((_idx, _idx), (1, 4))
      //_listMerged01 += CellRange((_idx, _idx), (6, 7))
      //_listMerged01 += CellRange((_idx, _idx), (8, 9))
      //_listMerged01 += CellRange((_idx, _idx), (10, 11))
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      //
      // LEER MANO DE OBRA
      val _parseManoObra = str("maob_descripcion") ~ get[Option[Int]](
        "maobpr_precio"
      ) ~ double("maobpr_rendimiento") map { case a1 ~ a2 ~ a3 => (a1, a2, a3) }
      val _manoObra = db.withConnection { implicit connection =>
        SQL(
          """SELECT mob1.maob_descripcion, mop1.maobpr_precio, mop1.maobpr_rendimiento from siap.mano_obra mob1
                  INNER JOIN siap.mano_obra_precio mop1 ON mop1.maob_id = mob1.maob_id
                  WHERE mop1.maobpr_anho = {anho} and mob1.empr_id = {empr_id} and 
                  mop1.cotr_tipo_obra = {cotr_tipo_obra} and mop1.cotr_tipo_obra_tipo = {cotr_tipo_obra_tipo}"""
        ).on(
          'anho -> orden.cotr_anho.get,
          'empr_id -> empresa.empr_id,
          'cotr_tipo_obra -> orden.cotr_tipo_obra.get,
          'cotr_tipo_obra_tipo -> orden.cotr_tipo_obra_tipo.get
        )
        .as(_parseManoObra *)
      }
      var _idx_inicial = _idx + 1
      var _idx_final = _idx + 1
      _manoObra.map { _m =>
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            _m._1,
            Some(1),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thick,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
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
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),               
          NumericCell(
            1,
            Some(5),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                horizontalAlignment = HA.Center,
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
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
                dataFormat = CellDataFormat("#,##0"),
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          NumericCell(
            _m._3,
            Some(7),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("#,##0.00"),
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(F" + (_idx + 1) + "* G" + (_idx + 1) + "/ H" + (_idx + 1) + ")",
            Some(8),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("#,##0"),
                borders = CellBorders(
                  leftStyle = CellBorderStyle.Thin,
                  leftColor = Color.Black,
                  rightStyle = CellBorderStyle.Thin,
                  rightColor = Color.Black,
                  topStyle = CellBorderStyle.Thin,
                  topColor = Color.Black,
                  bottomStyle = CellBorderStyle.Thin,
                  bottomColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(10),
            style = Some(
              CellStyle(
                dataFormat = CellDataFormat("@"),
                borders = CellBorders(
                  rightStyle = CellBorderStyle.Thick,
                  rightColor = Color.Black
                )
              )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _listMerged01 += CellRange((_idx, _idx), (1, 4))
        //_listMerged01 += CellRange((_idx, _idx), (6, 7))
        //_listMerged01 += CellRange((_idx, _idx), (8, 9))
        //_listMerged01 += CellRange((_idx, _idx), (10, 11))
        _listMerged01 += CellRange((_idx, _idx), (9, 10))
        _idx += 1
        _idx_final += 1
      }
      _idx_final -= 1
      // Subtotal C (Mano Obra)
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "SUBTOTAL (C)",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              font = _fuenteSubTotalTitulo,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black,
                topStyle = CellBorderStyle.Thin,
                topColor = Color.Black,
                bottomStyle = CellBorderStyle.Thin,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _listMerged01 += CellRange((_idx, _idx), (9, 10))
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                rightStyle = CellBorderStyle.Thick,
                rightColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        )
      )
      _idx += 1
      _listRow01 += com.norbitltd.spoiwo.model.Row(
        StringCell(
          "",
          Some(1),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thick,
                leftColor = Color.Black,
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        FormulaCell(
          "SUM(I" + (_idx_inicial) + ":I" + (_idx_final) + ")",
          Some(9),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("#,##0"),
              font = _fuenteSubTotalMoneda,
              borders = CellBorders(
                leftStyle = CellBorderStyle.Thin,
                leftColor = Color.Black,
                bottomStyle = CellBorderStyle.Thick,
                bottomColor = Color.Black
              )
            )
          ),
          CellStyleInheritance.CellThenRowThenColumnThenSheet
        ),
        StringCell(
          "",
          Some(10),
          style = Some(
            CellStyle(
              dataFormat = CellDataFormat("@"),
              horizontalAlignment = HA.Center,
              borders = CellBorders(
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
      _idx    
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_material(
      empresa: Empresa,
      orden: orden_trabajo_cobro,
      barrio: Barrio,
      unit_codigo: String,
      _listRow01: ListBuffer[com.norbitltd.spoiwo.model.Row],
      _listMerged01: ListBuffer[CellRange],
      _idx01: Int,
      _listCuadroGeneralUnitario: ListBuffer[
        (String, String, String, Double, String, String, String, String)
      ],
      elem_id: Option[Int]    
  ) = {

  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_3_unitarios_mano_obra(
      empresa: Empresa,
      orden: orden_trabajo_cobro,
      barrio: Barrio,
      unit_codigo: String,
      _listRow01: ListBuffer[com.norbitltd.spoiwo.model.Row],
      _listMerged01: ListBuffer[CellRange],
      _idx01: Int,
      _listCuadroGeneralUnitario: ListBuffer[
        (String, String, String, Double, String, String, String, String)
      ],
      elem_id: Option[Int]    
  ) = {

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
        properties = SheetProperties(autoBreaks = true),
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

  def siap_orden_trabajo_cobro_modernizacion_hoja_98_resumen_material(
      empresa: Empresa,
      orden: orden_trabajo_cobro,
      _listCuadroGeneralUnitario: ListBuffer[
        (String, String, String, Double, String, String, String, String)
      ]
  ): Sheet = {
    db.withConnection { implicit connection =>
      var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
      var _listMerged01 = new ListBuffer[CellRange]()
      val _fuenteNegrita = Font(
        height = 10.points,
        fontName = "Liberation Sans",
        bold = true,
        italic = false,
        strikeout = false
      )
      val _prefijo_interventoria = generalService.buscarPorId(10, empresa.empr_id.get).get.gene_valor.get
      val _parseMaterial = str("elem_codigo") ~ str("elem_descripcion") ~ double(
        "even_cantidad"
      ) ~ get[Option[String]]("elpr_unidad") ~ get[Option[Double]](
        "elpr_precio"
      ) ~ int("elem_estado") map { case a1 ~ a2 ~ a3 ~ a4 ~ a5 ~ a6 => (a1, a2, a3, a4, a5, a6) }
/*       val _qmat = """select e1.elem_codigo, e1.elem_descripcion, sum(re1.even_cantidad_instalado) as even_cantidad, ep1.elpr_unidad, ep1.elpr_precio, e1.elem_estado 
            from siap.cobro_orden_trabajo cot1
            inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
            inner join siap.reporte r1 on r1.repo_id = cotr1.repo_id
            inner join siap.reporte_evento re1 on re1.repo_id = r1.repo_id and re1.aap_id = cotr1.aap_id
            inner join siap.elemento e1 on e1.elem_id = re1.elem_id
            inner join siap.unitario u1 on u1.unit_id = re1.unit_id
            left join siap.elemento_precio ep1 on ep1.elem_id = e1.elem_id and ep1.elpr_anho = extract(year from cot1.cotr_fecha)
           where cot1.empr_id = {empr_id} and cotr1.cotr_id = {cotr_id}
           group by 1,2,4,5,6
           order by 2""" */
      val _qmat = """select e1.elem_codigo, e1.elem_descripcion, cotm1.cotrma_unidad AS elpr_unidad, cotm1.cotrma_valor_unitario AS elpr_precio, e1.elem_estado, SUM(cotm1.cotrma_cantidad) AS even_cantidad from siap.cobro_orden_trabajo cot1
                         inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cot1.cotr_id
                         inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                         inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                         where cot1.empr_id = {empr_id} AND cot1.cotr_id = {cotr_id}
                         group by 1,2,3,4,5"""

      val _materiales = SQL(
        _qmat
      ).on(
          'empr_id -> empresa.empr_id,
          'cotr_id -> orden.cotr_id
        )
        .as(_parseMaterial *)
      val _cantidad = SQL(
        """select count(*) from siap.cobro_orden_trabajo_reporte cotr1
                              where cotr1.cotr_id = {cotr_id}"""
      ).on('cotr_id -> orden.cotr_id).as(scalar[Int].single)

      _listMerged01 += CellRange((4, 4), (0, 6))
      _listMerged01 += CellRange((5, 5), (0, 6))
      _listMerged01 += CellRange((6, 6), (0, 6))
      _listMerged01 += CellRange((7, 7), (0, 6))
      Sheet(
        name = "Resumen_Material",
        properties = SheetProperties(autoBreaks = true),
        rows = {
          val emptyRow = com.norbitltd.spoiwo.model
            .Row()
            .withCellValues("")
          _listRow01 += emptyRow
          _listRow01 += emptyRow
          _listRow01 += emptyRow
          _listRow01 += emptyRow
          _listRow01 += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "ALUMBRADO PUBLICO DE " + empresa.muni_descripcion.get,
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center,                    
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
          _listRow01 += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "ORDEN DE TRABAJO No. " + _prefijo_interventoria + "-" + orden.cotr_consecutivo.get,
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center,                    
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
          _listRow01 += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "MODERNIZACION",
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center,                    
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
          _listRow01 += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                "VERIFICACION DEL MATERIAL ENTREGADO EN LA OBRA",
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@"),
                    horizontalAlignment = HA.Center,                    
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
          _listRow01 += com.norbitltd.spoiwo.model
            .Row(
              StringCell(
                orden.cotr_direccion.get,
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
            .withHeight(30.points)
          _listRow01 += com.norbitltd.spoiwo.model
            .Row()
            .withCellValues(
              "Código",
              "Descripción",
              "Cantidad",
              "Unidad",
              "Precio",
              "Cantidad Unitario",
              "Total Unitario",
              "TOTAL"
            )
          var _idx = 11
          _materiales.map {
            _m =>
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  _m._1,
                  Some(0),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@"),
                      fillPattern = CellFill.Solid,
                      fillForegroundColor = _m._6 match { case 2 => Color.LightBlue case _ => Color.White },
                      fillBackgroundColor = _m._6 match { case 2 => Color.White case _ => Color.Black }
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  _m._2,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@"),
                      fillPattern = CellFill.Solid,
                      fillForegroundColor = _m._6 match { case 2 => Color.LightBlue case _ => Color.White },
                      fillBackgroundColor = _m._6 match { case 2 => Color.White case _ => Color.Black }
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._3,
                  Some(2),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00"),
                      fillPattern = CellFill.Solid,
                      fillForegroundColor = _m._6 match { case 2 => Color.LightBlue case _ => Color.White },
                      fillBackgroundColor = _m._6 match { case 2 => Color.White case _ => Color.Black }
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  _m._4 match {
                    case Some(v) => v
                    case None    => ""
                  },
                  Some(3),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("@"),
                      fillPattern = CellFill.Solid,
                      fillForegroundColor = _m._6 match { case 2 => Color.LightBlue case _ => Color.White },
                      fillBackgroundColor = _m._6 match { case 2 => Color.White case _ => Color.Black }

                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
                  _m._5 match {
                    case Some(v) => v
                    case None    => 0.0
                  },
                  Some(4),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0"),
                      fillPattern = CellFill.Solid,
                      fillForegroundColor = _m._6 match { case 2 => Color.LightBlue case _ => Color.White },
                      fillBackgroundColor = _m._6 match { case 2 => Color.White case _ => Color.Black }
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                NumericCell(
/*                   if (_m._2.startsWith("FOTOCELDA")) {
                    if (_m._3 == _cantidad){
                      _m._3 / _cantidad
                    } else if ( _m._3 == 1) {
                      _m._3
                    } else {
                      1.00
                    }
                  } else {
                    _m._3 / _cantidad,
                  } */
                  _m._3 / _cantidad,
                  Some(5),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0.00"),
                      fillPattern = CellFill.Solid,
                      fillForegroundColor = _m._6 match { case 2 => Color.LightBlue case _ => Color.White },
                      fillBackgroundColor = _m._6 match { case 2 => Color.White case _ => Color.Black }
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(E" + _idx + "*F" + _idx + ")",
                  Some(6),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0"),
                      fillPattern = CellFill.Solid,
                      fillForegroundColor = _m._6 match { case 2 => Color.LightBlue case _ => Color.White },
                      fillBackgroundColor = _m._6 match { case 2 => Color.White case _ => Color.Black }
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(C" + _idx + "*E" + _idx + ")",
                  Some(7),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0"),
                      fillPattern = CellFill.Solid,
                      fillForegroundColor = _m._6 match { case 2 => Color.LightBlue case _ => Color.White },
                      fillBackgroundColor = _m._6 match { case 2 => Color.White case _ => Color.Black }
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )                
              )
              _idx += 1
          }
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "Costo Unitario Material",
              Some(5),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(G11:" + "G" + (_idx - 1) + ")",
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(H11:" + "H" + (_idx - 1) + ")",
              Some(7),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("#,##0")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )            
          )
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "PEDRO PARRA CARREÑO",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center,
                  font = _fuenteNegrita
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "ING. SALOMON IGLESIAS IGLESIAS",
              Some(4),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center,
                  font = _fuenteNegrita
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listMerged01 += CellRange((_idx, _idx), (0,3))
          _listMerged01 += CellRange((_idx, _idx), (4,6))
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "Supervisor de Obra Isag",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "Director Técnico de Interventoria " + _prefijo_interventoria,
              Some(4),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center,                  
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listMerged01 += CellRange((_idx, _idx), (0,3))
          _listMerged01 += CellRange((_idx, _idx), (4,6))
          _idx += 1

          _listRow01 += emptyRow
          _idx += 1          
          _listRow01 += emptyRow
          _idx += 1          
          _listRow01 += emptyRow
          _idx += 1          
          _listRow01 += emptyRow
          _idx += 1          
          _listRow01 += emptyRow
          _idx += 1          

          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "TOTAL CUADRO GENERAL",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _idx += 1     
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            ).withCellValues("","Cantidad","Valor","Total")
          _idx += 1
          var _idx_inicial = _idx     
          _listCuadroGeneralUnitario.map { _m =>
              _listRow01 += com.norbitltd.spoiwo.model.Row(
                NumericCell(
                  _m._4,
                  Some(1),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0"),
                      horizontalAlignment = HA.Center
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  _m._6,
                  Some(2),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0"),
                      horizontalAlignment = HA.Right
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                FormulaCell(
                  "SUM(B" + _idx + "*C" + _idx + ")",
                  Some(3),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0"),
                      horizontalAlignment = HA.Right
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )                
              )
              _idx += 1
            }
            _listRow01 += com.norbitltd.spoiwo.model.Row(
                FormulaCell(
                  "SUM(D" + _idx_inicial + ":D" + (_idx - 1) + ")",
                  Some(3),
                  style = Some(
                    CellStyle(
                      dataFormat = CellDataFormat("#,##0"),
                      horizontalAlignment = HA.Right
                    )
                  ),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )                
              )
          _listRow01.toList
        },
        mergedRegions = _listMerged01.toList
      )
    }
  }

  def siap_orden_trabajo_cobro_modernizacion_hoja_99_generado(
      empresa: Empresa,
      orden: orden_trabajo_cobro,
      usua_id: Long
  ): Sheet = {
    db.withConnection { implicit connection =>
      val _usuario = usuarioService.buscarPorId(usua_id) match {
        case Some(u) => u.usua_nombre + " " + u.usua_apellido
        case None    => "Usuario no Registrado"
      }
      var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
      var _listMerged01 = new ListBuffer[CellRange]()
      Sheet(
        name = "Generado",
        properties = SheetProperties(autoBreaks = true),
        rows = {
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "Generado:",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            DateCell(
              Calendar.getInstance().getTime(),
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("YYYY-MM-DD HH:mm:ss")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "Usuario:",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              _usuario,
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@")
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listRow01.toList
        }
      )
    }
  }

  def siap_orden_trabajo_cobro_relacion(empr_id: Long, anho: Int, periodo: Int): Array[Byte] = {
    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged01 = new ListBuffer[CellRange]()
    val _emptyRow = com.norbitltd.spoiwo.model.Row()
    val empresa = empresaService.buscarPorId(empr_id) match {
      case Some(e) => e
      case None => Empresa(None, "", "", "", "", "", None, 0, 0, 0, 0, None, None)
    }
    val ordenes =  buscarPorEmpresaAnhoPeriodo(empresa.empr_id.get, anho, periodo)
    var _idx = 0
    val sheet1 =  Sheet(
        name = "Relación",
        properties = SheetProperties(autoBreaks = true),
        rows = {
          _listRow01 += _emptyRow
          _idx += 1
          _listRow01 += _emptyRow
          _idx += 1          
          _listRow01 += _emptyRow
          _idx += 1          
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "MUNICIPIO DE" + empresa.muni_descripcion.get,
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listMerged01 += CellRange((_idx, _idx), (0, 4))
          _idx += 1
          _listRow01 += _emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "ORDENES DE TRABAJO OBRAS DE EXPANSION Y COMPLEMENTARIAS AÑO " + anho.toString(),
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
          )
          _listMerged01 += CellRange((_idx, _idx), (0, 4))
          _idx += 1
          _listRow01 += _emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "PERIODO: " + Utility.mes(periodo) + " /" + anho.toString(),
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
          )
          _listMerged01 += CellRange((_idx, _idx), (0, 4))
          _idx += 1          
          _listRow01 += _emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "FECHA",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "NUMERO ORDEN",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)                  
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "DESCRIPCION",
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "BARRIO",
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "TOTAL",
              Some(4),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )            
          ).withHeight(30.points)
          _idx += 1
          ordenes.map { orden =>
                val _fecha = orden.cotr_fecha.get
                val _consecutivo = orden.cotr_consecutivo.get
                val _descripcion = orden.cotr_tipo_obra match {
                   case Some(2) => { "EXPANSION DE " + N2T.convertirLetras(orden.cotr_cantidad.get).toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + orden.cotr_luminaria_nueva.get + " DE " + (orden.cotr_potencia_nueva match { case Some(v) => v case None => "" }) + " W" }
                   case Some(6) => { "MODERNIZACION DE " + N2T
                                    .convertirLetras(orden.cotr_cantidad.get)
                                    .toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + (orden.cotr_luminaria_anterior match { case Some(v) => v case None => "" }) + " DE " + (orden.cotr_potencia_anterior match { case Some(v) => v case None => ""}) + "W" + " POR " + N2T
                                    .convertirLetras(orden.cotr_cantidad.get)
                                    .toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + orden.cotr_luminaria_nueva.get + " DE " + orden.cotr_potencia_nueva.get + " W" }
                  case _ => s""
                }
                val _barrio = orden.cotr_direccion match {
                  case Some(b) => b
                  case None => ""
                }
                val _total = siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                _listRow01 += com.norbitltd.spoiwo.model.Row(
                  DateCell(
                    _fecha.toDate(),
                    Some(0),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("MMMM DD / YYYY"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Left
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    _consecutivo,
                    Some(1),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("#########0"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Right
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    _descripcion,
                    Some(2),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Left
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    _barrio,
                    Some(3),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    _total,
                    Some(4),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("#,##0"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Right
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  )
                ).withHeight(40.points)
                _idx += 1
            }

            _listRow01 += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "",
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "SUM(E11:E" + _idx + ")",
                Some(4),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("#,#00")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
          _listRow01.toList
        },
        mergedRegions = _listMerged01.toList,
        columns = {
          var _listColumn = new ArrayBuffer[com.norbitltd.spoiwo.model.Column]()
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 0, width = new Width(15, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 1, width = new Width(15, WidthUnit.Character))            
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 2, width = new Width(60, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 3, width = new Width(40, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 4, width = new Width(15, WidthUnit.Character))
        _listColumn.toList            
        }
      )
    println("Escribiendo en el Stream")
    var os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Workbook(sheet1)
      .writeToOutputStream(os)
    println("Stream Listo")
    os.toByteArray
  }

  def siap_orden_trabajo_cobro_acta_redimensionamiento(empr_id: Long, anho: Int, periodo: Int, usua_id: Long): (Int, Double, Double, Double, Double, Double /*, Iterable[(String, String, String, String, String)]*/) = {
    //var _listData = new ListBuffer[(String, String, String, String, String)]
    val empresa = empresaService.buscarPorId(empr_id) match {
      case Some(e) => e
      case None => Empresa(None, "", "", "", "", "", None, 0, 0, 0, 0, None, None)
    }
    val ordenes =  buscarPorEmpresaAnhoPeriodo(empresa.empr_id.get, anho, periodo)
    var _subtotal_expansion = 0.0
    var _subtotal_modernizacion = 0.0
    var _subtotal_desmonte = 0.0
    var _subtotal_total = 0.0
    // validar si existe el acta
    var _existe_acta = false
    var _numero_acta = 0
    val _periodo = Calendar.getInstance()
    _periodo.set(Calendar.YEAR, anho)
    _periodo.set(Calendar.MONTH, (periodo-1))
    _periodo.set(Calendar.DAY_OF_MONTH, 1)
    _periodo.set(Calendar.DATE, _periodo.getActualMaximum(Calendar.DATE))

    var _fecha_corte = _periodo.clone().asInstanceOf[Calendar]
    var _fecha_corte_anterior = _fecha_corte.clone().asInstanceOf[Calendar]
    _fecha_corte_anterior.add(Calendar.MONTH, -1)    

    val _valor_acumulado_anterior = db.withTransaction { implicit connection =>
      val _periodo_previo = _fecha_corte_anterior.get(Calendar.MONTH) + 1
      val _anho_previo = _fecha_corte_anterior.get(Calendar.YEAR)
      println("anho previo: " + _anho_previo)
      println("periodo previo: " + _periodo_previo)
      val _valorOpt = SQL("""SELECT reco_valor FROM siap.redimensionamiento_control rc1 WHERE rc1.reco_anho = {anho} AND rc1.reco_periodo = {periodo} AND rc1.empr_id = {empr_id}""").
      on(
        'anho -> _anho_previo,
        'periodo -> _periodo_previo,
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Double].singleOpt)
      _valorOpt match {
        case Some(v) => v
        case None => 0.0
      }
    }    
    val _acre = db.withTransaction { implicit connection =>
        val _parseActa = int("acre_id") ~ int("acre_numero") map {
          case acre_id ~ acre_numero => (acre_id, acre_numero)
        }
        SQL("""SELECT ar.acre_id, ar.acre_numero FROM siap.acta_redimensionamiento ar 
                            WHERE ar.acre_anho = {anho} AND ar.acre_periodo = {periodo} AND ar.empr_id = {empr_id}""").
      on(
        'anho -> anho,
        'periodo -> periodo,
        'empr_id -> empr_id
      ).as(_parseActa.singleOpt)
    }
    val acre_id = _acre match {
      case Some(v) =>  _existe_acta = true 
                        _numero_acta = v._2
                        v._1
      case None =>  _existe_acta = false
                    val _numero = dataUtil.getConsecutivo(9)
                    _numero_acta = _numero
                    db.withTransaction { implicit connection => 
                      SQL("""INSERT INTO siap.acta_redimensionamiento(acre_numero, acre_anho, acre_periodo, acre_fecha, empr_id, usua_id)
                            VALUES({numero}, {anho}, {periodo}, {fecha}, {empr_id}, {usua_id})""").
                      on(
                        'numero -> _numero,
                        'anho -> anho,
                        'periodo -> periodo,
                        'fecha -> Calendar.getInstance().getTime(),
                        'empr_id -> empr_id,
                        'usua_id -> usua_id
                      ).executeInsert().get
                    }

    }
    ordenes.map { orden =>
      var _expansion = orden.cotr_tipo_obra match {
                      case Some(2) => siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                      case _ => 0.0
                    }
      var _modernizacion = orden.cotr_tipo_obra match {
                      case Some(6) => siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                      case _ => 0.0
                    }
      var _desmonteRet = orden.cotr_tipo_obra match {
                      case Some(6) => siap_orden_trabajo_cobro_calculo_desmonte(empresa, orden, acre_id, _existe_acta)
                      case _ => (0.0, (new ListBuffer[(String, Double, Double)]).toList)
                    }                                       

      val _desmonte = _desmonteRet._1
      val _total = _expansion + _modernizacion - _desmonte
      _subtotal_expansion += _expansion
      _subtotal_modernizacion += _modernizacion
      _subtotal_desmonte += _desmonte
      _subtotal_total += _total
/*       _listData += 
        (
                   ( 
                    "Orden de Trabajo -" + orden.cotr_consecutivo.get,
                    if (_expansion > 0.0) { "$" + formatter.format(_expansion) } else { "" },
                    if (_modernizacion > 0.0) { "$" + formatter.format(_modernizacion) } else { "" },
                    if (_desmonte > 0.0) { "-$" + formatter.format(_desmonte) } else { "" },
                    "$" + formatter.format(_total)
                  )
        ) */
      }
      (
        _numero_acta,
        _valor_acumulado_anterior,
        _subtotal_expansion,
        _subtotal_modernizacion,
        _subtotal_desmonte,
        _subtotal_total
        // , _listData.toList
      )
  }

  def siap_orden_trabajo_cobro_anexo_redimensionamiento(empr_id: Long, anho: Int, periodo: Int, usua_id: Long): (Int, Array[Byte]) = {
    var _listData = new ListBuffer[(String, Double, Double, Double, Double)]
    val empresa = empresaService.buscarPorId(empr_id) match {
      case Some(e) => e
      case None => Empresa(None, "", "", "", "", "", None, 0, 0, 0, 0, None, None)
    }
    val _prefijo_interventoria = generalService.buscarPorId(10, empresa.empr_id.get).get.gene_valor.get
    val ordenes =  buscarPorEmpresaAnhoPeriodo(empresa.empr_id.get, anho, periodo)
    val formatter = java.text.NumberFormat.getIntegerInstance
    var _subtotal_expansion = 0.0
    var _subtotal_modernizacion = 0.0
    var _subtotal_desmonte = 0.0
    var _subtotal_total = 0.0
    val _periodo = Calendar.getInstance()
    _periodo.set(Calendar.YEAR, anho)
    _periodo.set(Calendar.MONTH, (periodo-1))
    _periodo.set(Calendar.DAY_OF_MONTH, 1)
    _periodo.set(Calendar.DATE, _periodo.getActualMaximum(Calendar.DATE))

    var _fecha_corte = _periodo.clone().asInstanceOf[Calendar]
    var _fecha_corte_anterior = _fecha_corte.clone().asInstanceOf[Calendar]
    _fecha_corte_anterior.add(Calendar.MONTH, -1)    

    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listRow02 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged01 = new ListBuffer[CellRange]()
    var _listMerged02 = new ListBuffer[CellRange]()
    val _valor_acumulado_anterior = db.withTransaction { implicit connection =>
      val _periodo_previo = _fecha_corte_anterior.get(Calendar.MONTH) + 1
      val _anho_previo = _fecha_corte_anterior.get(Calendar.YEAR)
      println("anho previo: " + _anho_previo)
      println("periodo previo: " + _periodo_previo)
      val _valorOpt = SQL("""SELECT reco_valor FROM siap.redimensionamiento_control rc1 WHERE rc1.reco_anho = {anho} AND rc1.reco_periodo = {periodo} AND rc1.empr_id = {empr_id}""").
      on(
        'anho -> _anho_previo,
        'periodo -> _periodo_previo,
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Double].singleOpt)
      _valorOpt match {
        case Some(v) => v
        case None => 0.0
      }
    }

    val _valor_ipp_base = db.withTransaction { implicit connection =>
      SQL("""SELECT ucap_ipp_valor FROM siap.ucap_ipp ui1 WHERE ui1.ucap_ipp_anho = {anho} AND ui1.empr_id = {empr_id}""").
      on(
        'anho -> 2014,
        'periodo -> periodo,
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Double].singleOpt) match {
        case Some(v) => v
        case None => 0.0
      }
    }

    val _valor_ipp_anho_anterior = db.withTransaction { implicit connection =>
      val _valorOpt = SQL("""SELECT ucap_ipp_valor FROM siap.ucap_ipp ui1 WHERE ui1.ucap_ipp_anho = {anho} AND ui1.empr_id = {empr_id}""").
      on(
        'anho -> (anho - 1),
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Double].singleOpt)
      _valorOpt match {
        case Some(v) => v
        case None => 0.0
      }
    }
    val _numero_acta = db.withTransaction { implicit connection => 
      SQL("""SELECT ar.acre_numero FROM siap.acta_redimensionamiento ar 
                            WHERE ar.acre_anho = {anho} AND ar.acre_periodo = {periodo} AND ar.empr_id = {empr_id}""").
      on(
        'anho -> anho,
        'periodo -> periodo,
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Int].single)
    }
    val _sheet02 = Sheet(
      name = "Anexo 02",
      rows = {
        var _idx0 = 0
        _listRow02 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "ANEXO 02 - ACTA DE REDIMENSIONAMIENTO No." + _numero_acta,
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 10.points)
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _idx0 += 1
        _listRow02 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            Utility.fechaamesanho(Some(new DateTime(_fecha_corte.getTime()))),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 10.points)
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )        
        _idx0 += 1
        _listRow02 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "",
            Some(0),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"))
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _idx0 += 1
        ordenes.map { orden =>
          if (orden.cotr_tipo_obra.get == 6) {
            _listRow02 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "DESMONTE DE UCAPS MODERNIZADAS ODT ITF-" + orden.cotr_consecutivo.get,
              Some(0),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points)
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
            )
            _idx0 += 1
            _listRow02 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "UCAPS",
              Some(0),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "UND",
              Some(1),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VALOR UCAP DICIEMBRE 2014",
              Some(2),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VALOR IPP DICIEMBRE " + (_fecha_corte.get(Calendar.YEAR) - 1),
              Some(3),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VALOR IPP DICIEMBRE 2014",
              Some(4),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "VALOR UNITARIO UCAP PARA DESMONTAR",
              Some(5),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "TOTAL",
              Some(6),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )                          
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            ).withHeight(55.points)
            _idx0 += 1
          }
          var _expansion = orden.cotr_tipo_obra match {
                      case Some(2) => siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                      case _ => 0.0
                    }
          var _modernizacion = orden.cotr_tipo_obra match {
                      case Some(6) => siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                      case _ => 0.0
                    }
          var _desmonteRet = orden.cotr_tipo_obra match {
                      case Some(6) => siap_orden_trabajo_cobro_calculo_desmonte(empresa, orden, 0, true)
                      case _ => (0.0, (new ListBuffer[(String, Double, Double)]()).toList)
                    }
          if (orden.cotr_tipo_obra.get == 6) {                    
            var _idx01 = _idx0 + 1
            var _hay = false
            _desmonteRet._2.map { _m =>
            _hay = true
            _listRow02 += com.norbitltd.spoiwo.model.Row(
              StringCell(
                _m._1,
                Some(0),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("@"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )                            
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _m._2,
                Some(1),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )                            
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _m._3 * _valor_ipp_base / _valor_ipp_anho_anterior,
                Some(2),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _valor_ipp_anho_anterior,
                Some(3),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0.00"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              NumericCell(
                _valor_ipp_base,
                Some(4),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0.00"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "ROUND(C" + (_idx0 + 1) + "*D" +(_idx0 + 1) + "/E" + (_idx0 + 1) +",0)",
                Some(5),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "ROUND(B" + (_idx0 + 1) + "*F" +(_idx0 + 1)+",0)",
                Some(6),
                style = Some(
                          CellStyle(
                            dataFormat = CellDataFormat("#,##0"),
                            borders = CellBorders(
                              topStyle = CellBorderStyle.Thin,
                              topColor = Color.Black,
                              leftStyle = CellBorderStyle.Thin,
                              leftColor = Color.Black,
                              rightStyle = CellBorderStyle.Thin,
                              rightColor = Color.Black,
                              bottomStyle = CellBorderStyle.Thin,
                              bottomColor = Color.Black
                            )                            
                          )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
            )
            _idx0 += 1
            }
            if (_hay) {
            _listRow02 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "TOTAL",
              Some(0),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          font = Font(bold = true, height = 10.points),
                          wrapText = true,
                          verticalAlignment = VA.Center,
                          horizontalAlignment = HA.Center,
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
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
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
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
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
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
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
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
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
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
                          dataFormat = CellDataFormat("@"),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "ROUND(SUM(G"+ (_idx01) +":G" + (_idx0) + "),0)",
              Some(6),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("#,##0"),
                          font = Font(bold = true, height = 10.points),
                          borders = CellBorders(
                            topStyle = CellBorderStyle.Thin,
                            topColor = Color.Black,
                            leftStyle = CellBorderStyle.Thin,
                            leftColor = Color.Black,
                            rightStyle = CellBorderStyle.Thin,
                            rightColor = Color.Black,
                            bottomStyle = CellBorderStyle.Thin,
                            bottomColor = Color.Black
                          )
                        )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
            )
            _idx0 += 1
            }
            _listRow02 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "",
              Some(0),
              style = Some(
                        CellStyle(dataFormat = CellDataFormat("@"))
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
              )
            _idx0 += 1
          }    
          _expansion = _expansion
          _modernizacion = _modernizacion
          val _desmonte = _desmonteRet._1
          val _total = _expansion + _modernizacion - _desmonte
          _subtotal_expansion += _expansion
          _subtotal_modernizacion += _modernizacion
          _subtotal_desmonte += _desmonte
          _subtotal_total += _total
          _listData += 
            (
                   ( 
                    "Orden de Trabajo " + _prefijo_interventoria + "-" + orden.cotr_consecutivo.get,
                    _expansion,
                    _modernizacion,
                    _desmonte,
                    _total
                  )
            )
        }
        _listRow02.toList
      },
      columns = {
        var _listColumn = new ArrayBuffer[com.norbitltd.spoiwo.model.Column]()
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 0, width = new Width(60, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 1, width = new Width(8, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 2, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 3, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 4, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 5, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 6, width = new Width(19, WidthUnit.Character))
        _listColumn.toList        

      }
    )
    val _sheet01 = Sheet(
      name = "Anexo 01",
      rows = {
        var _idx = -1
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "ANEXO 01 - ACTA DE REDIMENSIONAMIENTO No." + _numero_acta,
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true)
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _idx += 1
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "REDIMENSIONAMIENTO DE LA INFRAESTRUCTURA DEL SISTEMA DE ALUMBRADO PUBLICO DE " + empresa.muni_descripcion.get,
            Some(0),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"),
                      wrapText = true,
                      verticalAlignment = VA.Center,
                      horizontalAlignment = HA.Center)
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "Expansión",
            Some(1),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "Obras complementarias de expansión",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(3),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"))
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "TOTAL REDIMENSIONAMIENTO DEL SISTEMA",
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center,                        
                        wrapText = true,
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
        )
        _idx += 1
        _listMerged01 += CellRange((_idx, _idx + 1), (0, 0))
        _listMerged01 += CellRange((_idx, _idx + 1), (1, 1))
        _listMerged01 += CellRange((_idx, _idx), (2, 3))
        _listMerged01 += CellRange((_idx, _idx + 1), (4, 4))
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "",
            Some(0),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"))
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(1),
            style = Some(
                      CellStyle(dataFormat = CellDataFormat("@"))
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "VR UCAPS Instaladas",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = true,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "VR UCAPS Desmontadas",
            Some(3),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = true,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        ).withHeight(40.points)
        _idx += 1
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "Redimensionamiento de la Infraestructura de Alumbrado Público Desde el 1 de Enero de 2015 hasta el " + Utility.fechaatextosindia(Some(new DateTime(_fecha_corte.getTime()))),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid,
                        wrapText = true
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(1),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),            
          StringCell(
            "",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(3),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          NumericCell(
            _valor_acumulado_anterior,
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid,
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        ).withHeight(30.points)
        _idx += 1
        _listData.map { _data =>
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              _data._1,
              Some(0),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("@"),
                          wrapText = true,
                          horizontalAlignment = HA.Left,
                          verticalAlignment = VA.Top
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _data._2,
              Some(1),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("$#,##0")
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _data._3,
              Some(2),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("$#,##0")
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            NumericCell(
              _data._4,
              Some(3),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("$#,##0")
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            FormulaCell(
              "SUM(B" + (_idx + 2) + "+C" + (_idx + 2) + "-D" + (_idx + 2) + ")",
              Some(4),
              style = Some(
                        CellStyle(
                          dataFormat = CellDataFormat("$#,##0")
                        )
                      ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _idx += 1
        }
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "Subtotal Redimensionamiento del Mes al " + Utility.fechaatextosindia(Some(new DateTime(_fecha_corte.getTime()))),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        wrapText = true,
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(B5"+ ":B" + (_idx + 1) + ")",
            Some(1),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 10.points),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid                        
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(C5"+ ":C" + (_idx + 1) + ")",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 10.points),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid                        
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(D5"+ ":D" + (_idx + 1) + ")",
            Some(3),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 10.points),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "SUM(E5"+ ":E" + (_idx + 1) + ")",
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 10.points),
                        fillForegroundColor = Color.LightGrey,
                        fillPattern = CellFill.Solid
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        )
        _idx += 1
        _listRow01 += com.norbitltd.spoiwo.model.Row(
          StringCell(
            "Total redimensionamiento de la Infraestructura del sistema de Alumbrado Público desde el 1 de Enero de 2015 Hasta el " + Utility.fechaatextosindia(Some(new DateTime(_fecha_corte.getTime()))),
            Some(0),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        wrapText = true,
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid                        
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(1),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(2),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid                        
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          StringCell(
            "",
            Some(3),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        font = Font(bold = true, height = 9.points),
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid
                      )
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          ),
          FormulaCell(
            "E4+"+ "E" + (_idx + 1),
            Some(4),
            style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("$#,##0"),
                        font = Font(bold = true, height = 11.points),
                        fillForegroundColor = Color.Grey,
                        fillPattern = CellFill.Solid
                      )                      
            ),
            CellStyleInheritance.CellThenRowThenColumnThenSheet
          )
        ).withHeight(35.points)
        _listRow01.toList
      },
      mergedRegions = _listMerged01.toList,
      columns = {
        var _listColumn = new ArrayBuffer[com.norbitltd.spoiwo.model.Column]()
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 0, width = new Width(60, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 1, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 2, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 3, width = new Width(14, WidthUnit.Character))
        _listColumn += com.norbitltd.spoiwo.model
          .Column(index = 4, width = new Width(19, WidthUnit.Character))
        _listColumn.toList
      }
    )
    println("Escribiendo en el Stream")
    var os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Workbook(_sheet01, _sheet02)
      .writeToOutputStream(os)
    println("Stream Listo")
    (_numero_acta, os.toByteArray())
  }

  def siap_orden_trabajo_cobro_calculo_desmonte(empresa: Empresa, orden: orden_trabajo_cobro, acre_id: Long, _existe_acta: Boolean): (Double, Iterable[(String, Double, Double)]) = {
    db.withTransaction { implicit connection =>
      val _parseMaterial = int("cotr_id") ~ int("elem_id") ~ str("elem_descripcion") ~ double("cantidad") ~ double("valor") map {
        case cotr_id ~ elem_id ~ elem_descripcion ~ cantidad ~ valor => (cotr_id, elem_id, elem_descripcion, cantidad, valor)
      }
       val _anho_anterior = orden.cotr_anho match {
                              case Some(a) => a - 1
                              case None => Calendar.getInstance().get(Calendar.YEAR) - 1
      }
       val _ipp2014 = SQL("""SELECT ucap_ipp_valor FROM siap.ucap_ipp WHERE ucap_ipp_anho = {anho}""").on('anho -> 2014).as(SqlParser.double("ucap_ipp_valor").single)
       val _ippActual = SQL("""SELECT ucap_ipp_valor FROM siap.ucap_ipp WHERE ucap_ipp_anho = {anho}""").on('anho -> _anho_anterior).as(SqlParser.double("ucap_ipp_valor").single)
       val _material = SQL("""select cotr1.cotr_id, re1.elem_id, e1.elem_descripcion as elem_descripcion , re1.even_cantidad_retirado as cantidad, (uiv.ucap_ipp_valor_valor * {ippActual}/{ipp2014}) as valor from siap.cobro_orden_trabajo cot1
                              inner join siap.cobro_orden_trabajo_reporte cotr1 on cotr1.cotr_id = cot1.cotr_id 
                              inner join siap.reporte_evento re1 on re1.repo_id = cotr1.repo_id and re1.aap_id = cotr1.aap_id and re1.even_estado < 8 and re1.even_cantidad_retirado > 0
                              inner join siap.elemento e1 on e1.elem_id = re1.elem_id and e1.tiel_id IN (1,6,9)
                              inner join siap.ucap_ipp_valor uiv on uiv.elem_id = e1.elem_id and uiv.ucap_ipp_valor_anho = 2014
                              where cot1.cotr_id = {cotr_id}
                              order by 1,2""")
                              .on(
                                'cotr_id -> orden.cotr_id,
                                'ipp2014 -> _ipp2014,
                                'ippActual -> _ippActual
                              ).as(_parseMaterial *)                         
      var _desmonte = 0.0
      _material.map { _m =>
        _desmonte += _m._4 * _m._5
        if (!_existe_acta) {
          SQL("""INSERT INTO siap.acta_redimensionamiento_detalle (acre_id, cotr_id, elem_id, acrede_cantidad, acrede_valor) 
                 VALUES ({acre_id}, {cotr_id}, {elem_id}, {cantidad}, {valor})""")
          .on(
            'acre_id -> acre_id,
            'cotr_id -> _m._1,
            'elem_id -> _m._2,
            'cantidad -> _m._4,
            'valor -> _m._4
          ).executeUpdate()
        }
      }

      // Buscar material para reporte anexo 2
      val _parseMaterialAnexo = str("elem_descripcion") ~ double("cantidad") ~ double("valor") map {
        case elem_descripcion ~ cantidad ~ valor => (elem_descripcion, cantidad, valor)
      }
      val _listMaterial = SQL(
                """SELECT e1.elem_descripcion as elem_descripcion, SUM(ard1.acrede_cantidad) as cantidad, (uiv.ucap_ipp_valor_valor * {ippActual}/{ipp2014}) as valor from siap.acta_redimensionamiento_detalle ard1
                inner join siap.elemento e1 on e1.elem_id = ard1.elem_id and e1.tiel_id in (1,6,9)
                left join siap.ucap_ipp_valor uiv on uiv.elem_id = e1.elem_id and uiv.ucap_ipp_valor_anho = 2014
                where ard1.cotr_id = {cotr_id}
                group by 1,3
                order by 1,2""")
                              .on(
                                'cotr_id -> orden.cotr_id,
                                'ipp2014 -> _ipp2014,
                                'ippActual -> _ippActual
                              ).as(_parseMaterialAnexo *)
      //
      (_desmonte, _listMaterial)
    }
  }

  def siap_orden_trabajo_cobro_relacion_factura(empr_id: Long): Array[Byte] = {
    var _listRow01 = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
    var _listMerged01 = new ListBuffer[CellRange]()
    val _emptyRow = com.norbitltd.spoiwo.model.Row()
    val empresa = empresaService.buscarPorId(empr_id) match {
      case Some(e) => e
      case None => Empresa(None, "", "", "", "", "", None, 0, 0, 0, 0, None, None)
    }
    val ordenes =  buscarPorEmpresaAnhoPeriodo(empr_id, 0, 0)
    var _idx = 0
    db.withConnection { implicit connection =>
    val sheet1 =  Sheet(
        name = "Relación_Factura",
        properties = SheetProperties(autoBreaks = true),
        rows = {
          _listRow01 += _emptyRow
          _idx += 1
          _listRow01 += _emptyRow
          _idx += 1          
          _listRow01 += _emptyRow
          _idx += 1          
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "MUNICIPIO DE" + empresa.muni_descripcion.get,
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )
          )
          _listMerged01 += CellRange((_idx, _idx), (0, 4))
          _idx += 1
          _listRow01 += _emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "ORDENES DE TRABAJO OBRAS DE EXPANSION Y COMPLEMENTARIAS",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
          )
          _listMerged01 += CellRange((_idx, _idx), (0, 4))
          _idx += 1
          _listRow01 += _emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "TODOS LOS PERIODOS",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
          )
          _listMerged01 += CellRange((_idx, _idx), (0, 4))
          _idx += 1          
          _listRow01 += _emptyRow
          _idx += 1
          _listRow01 += com.norbitltd.spoiwo.model.Row(
            StringCell(
              "FECHA",
              Some(0),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  wrapText = java.lang.Boolean.TRUE,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "NUMERO ORDEN",
              Some(1),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)                  
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "DESCRIPCION",
              Some(2),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "BARRIO",
              Some(3),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "TOTAL",
              Some(4),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "FACTURA",
              Some(5),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "FECHA FACTURA",
              Some(6),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "AÑO FACTURADO",
              Some(7),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            ),
            StringCell(
              "PERIODO FACTURADO",
              Some(8),
              style = Some(
                CellStyle(
                  dataFormat = CellDataFormat("@"),
                  wrapText = java.lang.Boolean.TRUE,
                  horizontalAlignment = HA.Center,
                  verticalAlignment = VA.Center,
                  font = Font(bold = true)
                )
              ),
              CellStyleInheritance.CellThenRowThenColumnThenSheet
            )                        
          ).withHeight(30.points)
          _idx += 1
          val _parse_factura = get[Option[String]]("factura") ~ get[Option[DateTime]]("cofa_fecha") ~get[Option[Int]]("cofa_anho") ~ get[Option[Int]]("cofa_periodo") map {
            case factura ~ fecha ~ anho ~ periodo =>
              (factura, fecha, anho, periodo)
          }
          ordenes.map { orden =>
                
                val _factura = SQL("""SELECT CONCAT(cofa1.cofa_prefijo, cofa1.cofa_factura, cofa1.cofa_sufijo) as factura, cofa_fecha, cofa_anho, cofa_periodo FROM siap.cobro_orden_trabajo cotr1 
                                      LEFT JOIN siap.cobro_factura_orden_trabajo cofaot1 ON cofaot1.cotr_id = cotr1.cotr_id
                                      LEFT JOIN siap.cobro_factura cofa1 ON cofa1.cofa_id = cofaot1.cofa_id
                                      WHERE cotr1.empr_id = 1 and cotr1.cotr_id = {cotr_id} and cofa1.cofa_estado = 1""").
                                      on(
                                        'cotr_id -> orden.cotr_id
                                      ).as(_parse_factura.singleOpt)
                var _facno = ""
                var _facfecha = ""
                var _facanho = ""
                var _facperiodo = ""
                _factura match {
                  case Some(f) =>
                    _facno = f._1.getOrElse("")
                    _facfecha = f._2 match { case Some(f) => f.toString("yyyy-MM-dd") case None => "" }
                    _facanho = f._3.getOrElse("").toString
                    _facperiodo = f._4.getOrElse("").toString
                  case None =>
                    _facno = ""
                    _facfecha = ""
                    _facanho = ""
                    _facperiodo = ""
                }
                val _fecha = orden.cotr_fecha.get
                val _consecutivo = orden.cotr_consecutivo.get
                val _descripcion = orden.cotr_tipo_obra match {
                   case Some(2) => { "EXPANSION DE " + N2T.convertirLetras(orden.cotr_cantidad.get).toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + orden.cotr_luminaria_nueva.get + " DE " + orden.cotr_potencia_nueva.get + " W" }
                   case Some(6) => { "MODERNIZACION DE " + N2T
                                    .convertirLetras(orden.cotr_cantidad.get)
                                    .toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + (orden.cotr_luminaria_anterior match { case Some(v) => v case None => "" }) + " DE " + orden.cotr_potencia_anterior.get + "W" + " POR " + N2T
                                    .convertirLetras(orden.cotr_cantidad.get)
                                    .toUpperCase + " (" + orden.cotr_cantidad.get + ") LUMINARIAS " + orden.cotr_luminaria_nueva.get + " DE " + orden.cotr_potencia_nueva.get + " W" }
                  case _ => s""
                }
                val _barrio = orden.cotr_direccion match {
                  case Some(b) => b
                  case None => ""
                }
                val _total = siap_orden_trabajo_cobro_calculo_total(empresa, orden)
                _listRow01 += com.norbitltd.spoiwo.model.Row(
                  DateCell(
                    _fecha.toDate(),
                    Some(0),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("MMMM DD / YYYY"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Left
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    _consecutivo,
                    Some(1),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("#########0"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Right
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    _descripcion,
                    Some(2),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Left
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    _barrio,
                    Some(3),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Center
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  NumericCell(
                    _total,
                    Some(4),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("#,##0"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Right
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    _facno,
                    Some(5),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Right
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    _facfecha,
                    Some(6),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Right
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    _facanho,
                    Some(7),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Right
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  ),
                  StringCell(
                    _facperiodo,
                    Some(8),
                    style = Some(
                      CellStyle(
                        dataFormat = CellDataFormat("@"),
                        wrapText = java.lang.Boolean.TRUE,
                        verticalAlignment = VA.Center,
                        horizontalAlignment = HA.Right
                      )
                    ),
                    CellStyleInheritance.CellThenRowThenColumnThenSheet
                  )
                ).withHeight(40.points)
                _idx += 1
            }

            _listRow01 += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "",
                Some(0),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(1),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(2),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "",
                Some(3),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("@")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              FormulaCell(
                "SUM(E11:E" + _idx + ")",
                Some(4),
                style = Some(
                  CellStyle(
                    dataFormat = CellDataFormat("#,#00")
                  )
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
          _listRow01.toList
        }, 
        mergedRegions = _listMerged01.toList,
        columns = {
          var _listColumn = new ArrayBuffer[com.norbitltd.spoiwo.model.Column]()
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 0, width = new Width(15, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 1, width = new Width(15, WidthUnit.Character))            
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 2, width = new Width(60, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 3, width = new Width(40, WidthUnit.Character))
          _listColumn += com.norbitltd.spoiwo.model
            .Column(index = 4, width = new Width(15, WidthUnit.Character))
        _listColumn.toList            
        }
      )
    println("Escribiendo en el Stream")
    var os: ByteArrayOutputStream = new ByteArrayOutputStream()
    Workbook(sheet1)
      .writeToOutputStream(os)
    println("Stream Listo")
    os.toByteArray
    }
  }

  def siap_orden_trabajo_cobro_calculo_total(empresa: Empresa, orden: orden_trabajo_cobro) = {
    var _total = 0D
    var _listCuadroGeneralUnitario = new ListBuffer[(String, Double, Double, Double, Double)]()
    db.withConnection{ implicit connection =>
    var _query = """select distinct u1.unit_codigo
                      from siap.cobro_orden_trabajo cot1
                      inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cot1.cotr_id
                      inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                      inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                      where cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and e1.elem_estado = 1
          """
    orden.cotr_tipo_obra match {
      case Some(v) => v match {
            case 6 => _query += """ union all
                         select '1.04' as unit_codigo"""
            case _ => _query += ""
        }
      case None => _query +=  ""
    }
    _query += " order by unit_codigo"
    var _unitarios = SQL(_query)
        .on(
          'empr_id -> orden.empr_id,
          'cotr_id -> orden.cotr_id
        )
        .as(SqlParser.str("unit_codigo").*)

    _unitarios.map { unitario =>
        if (unitario == "1.01") {
          /// buscar si existe más de una ucap en el unitario para separar valores
          val _elements =
            SQL("""select distinct e1.elem_id, e1.elem_descripcion
                          from siap.cobro_orden_trabajo cot1
                          inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cot1.cotr_id
                          inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                          inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                          where cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and u1.unit_codigo = {unitario} and e1.ucap_id = 1
                          order by e1.elem_id""")
              .on(
                'empr_id -> orden.empr_id,
                'cotr_id -> orden.cotr_id,
                'unitario -> "1.01"
              )
              .as(SqlParser.int("elem_id").*)
          _elements.map { e =>
            _listCuadroGeneralUnitario += util_cobro_orden_trabajo_calculo(
              empresa,
              orden,
              unitario,
              Some(e)
            )
          }
        } else {
          _listCuadroGeneralUnitario += util_cobro_orden_trabajo_calculo(
            empresa,
            orden,
            unitario,
            None
          )
        }
      }
    }
    var _subtotal = 0D
    _listCuadroGeneralUnitario.map { _item =>
      _subtotal += (_item._2 * (_item._3 + _item._4 + _item._5))
    }
    val _mano_ingenieria = util_cobro_orden_trabajo_mano_ingenieria(empresa, orden, _subtotal)
    _total = _subtotal + _mano_ingenieria

    _total
  }

  def util_cobro_orden_trabajo_calculo(empresa: Empresa, orden: orden_trabajo_cobro, unit_codigo: String, elem_id: Option[Int]): (String, Double, Double, Double, Double) = {
    var _unitario_total = 0D
    val _cantidad = util_cobro_orden_trabajo_cantidad(empresa, orden, unit_codigo, elem_id)
    val _herramienta = util_cobro_orden_trabajo_herramienta(empresa, orden, unit_codigo, elem_id)
    val _material = util_cobro_orden_trabajo_material(empresa, orden, unit_codigo, elem_id, _cantidad)
    val _cantidad103 = _material._2
    val _mano_obra = util_cobro_orden_trabajo_mano_obra(empresa, orden, unit_codigo, elem_id, _cantidad103)
    (unit_codigo, _cantidad, _herramienta, _material._1, _mano_obra)
  }  

  def util_cobro_orden_trabajo_cantidad(empresa: Empresa, orden: orden_trabajo_cobro, unit_codigo: String, elem_id: Option[Int]) = {
// Evaluar la Cantidad del Unitario
    db.withConnection { implicit connection =>
      var _ucap_id = unit_codigo match {
        case "1.01" => 1
        case "1.02" => 2
        case "1.03" => 3
        case "1.04" => 4
        case "1.05" => 5
        case "1.06" => 6
        case "1.07" => 7
        case "1.08" => 8
        case "1.09" => 9
        case "1.10" => 10
        case "1.11" => 11
        case "1.12" => 12
        case _      => 0
      }      
      var _cantidad_item = elem_id match {
        case Some(elem_id) =>
          val _qcant = """SELECT SUM(cotrma_cantidad) FROM siap.cobro_orden_trabajo cot1
                        INNER JOIN siap.cobro_orden_trabajo_material cotrma1 ON cotrma1.cotr_id = cot1.cotr_id
                        WHERE cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and cotrma1.elem_id = {elem_id} AND cotrma1.unit_id = {unit_id}"""
          SQL(
            _qcant
          ).on(
              'empr_id -> empresa.empr_id,
              'cotr_id -> orden.cotr_id,
              // 'unit_codigo -> unit_codigo,
              //'ucap_id -> _ucap_id,
              'unit_id -> _ucap_id,
              'elem_id -> elem_id
            )
            .as(SqlParser.scalar[Double].singleOpt)

        case None =>
          val _qcant = """SELECT SUM(cotrma_cantidad) FROM siap.cobro_orden_trabajo cot1
                        INNER JOIN siap.cobro_orden_trabajo_material cotrma1 ON cotrma1.cotr_id = cot1.cotr_id
                        INNER JOIN siap.elemento e1 ON e1.elem_id = cotrma1.elem_id
                        WHERE cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and e1.ucap_id = {ucap_id} AND cotrma1.aap_id is not null"""
          SQL(
            _qcant
          ).on(
              'empr_id -> empresa.empr_id,
              'cotr_id -> orden.cotr_id,
              // 'unit_codigo -> unit_codigo,
              'ucap_id -> _ucap_id
            )
            .as(SqlParser.scalar[Double].singleOpt)      }

       var _cantidad_xls = _cantidad_item match {
        case Some(v) => v
        case None  => orden.cotr_cantidad.get.toDouble
       }

      if (unit_codigo == "1.03") {
        _cantidad_xls = 1.0
      }
             
       _cantidad_xls
    }
  }

  def util_cobro_orden_trabajo_herramienta(empresa: Empresa, orden: orden_trabajo_cobro, unit_codigo: String, elem_id: Option[Int]) = {
      val _herramientaParser = int("math_codigo") ~ str("math_descripcion") ~ double(
        "mathpr_precio"
      ) ~ bool("mathpr_es_porcentaje") ~ double("mathpr_cantidad") ~ double(
        "mathpr_rendimiento"
      ) map {
        case math_codigo ~ math_descripcion ~ mathpr_precio ~ mathpr_es_porcentaje ~ mathpr_cantidad ~ mathpr_rendimiento =>
          (
            math_codigo,
            math_descripcion,
            mathpr_cantidad,
            mathpr_precio,
            mathpr_rendimiento,
            mathpr_es_porcentaje
          )
      }
      var _herramientaLista = db.withConnection { implicit connection =>
        SQL(
        """select mth1.math_codigo, mth1.math_descripcion, mthp1.mathpr_precio, mthp1.mathpr_es_porcentaje, mthp1.mathpr_cantidad, mthp1.mathpr_rendimiento from siap.mano_transporte_herramienta mth1
              left join siap.mano_transporte_herramienta_precio mthp1 on mthp1.math_id = mth1.math_id 
              where mthp1.mathpr_anho = {anho} and mth1.empr_id = {empr_id} and
              mthp1.cotr_tipo_obra = {cotr_tipo_obra} and mthp1.cotr_tipo_obra_tipo = {cotr_tipo_obra_tipo}
              order by mth1.math_id ASC"""
      ).on(
          'anho -> orden.cotr_anho.get,
          'empr_id -> empresa.empr_id,
          'cotr_tipo_obra -> orden.cotr_tipo_obra.get,
          'cotr_tipo_obra_tipo -> orden.cotr_tipo_obra_tipo.get
        )
        .as(_herramientaParser.*)
      }
      var _total=0D
      _herramientaLista.map { _m =>
/*         if ((_h._1 == 2 && (orden.aaus_id.get == 3 || orden.aaus_id.get == 4)) || (_h._1 == 3 && (orden.aaus_id.get != 3 && orden.aaus_id.get != 4)) || (orden.cotr_tipo_obra_tipo.get.trim == '3')) {
          } else {        
            _total += ((_h._3 * _h._4) / _h._5)
          } */
        if (
            (_m._1 != 2 && _m._1 != 3) ||
            (_m._1 == 3 && orden.cotr_tipo_obra.get == 2 && orden.cotr_tipo_obra_tipo.get.trim.toInt == 3) ||
            (_m._1 == 2 && (orden.aaus_id.get != 3 && orden.aaus_id.get != 4) && (!(orden.cotr_tipo_obra.get == 2 && orden.cotr_tipo_obra_tipo.get.trim.toInt == 3))) ||
            (_m._1 == 3 && (orden.aaus_id.get == 3 || orden.aaus_id.get == 4))          
        ) {
          _total += ((_m._3 * _m._4) / _m._5)
        }
      }
      _total
  }

  def util_cobro_orden_trabajo_material(empresa: Empresa, orden: orden_trabajo_cobro, unit_codigo: String, elem_id: Option[Int], _cantidad_xls: Double) = {
    db.withConnection { implicit connection =>
      val _parseMaterial = str("elem_descripcion") ~ get[Option[String]](
        "elpr_unidad"
      ) ~ get[Option[Int]]("elpr_precio") ~ double("even_cantidad") map {
        case a1 ~ a2 ~ a3 ~ a4 => (a1, a2, a3, a4)
      }
      val _materiales = elem_id match {
        case Some(elem_id) =>
          // Buscar Luminarias Correspondientes para el material correcto
          var _luminarias = SQL(
            """select cotm1.aap_id
                          from siap.cobro_orden_trabajo cot1
                          inner join siap.cobro_orden_trabajo_material cotm1 on cotm1.cotr_id = cot1.cotr_id 
                          inner join siap.elemento e1 ON e1.elem_id = cotm1.elem_id
                          inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                          where cot1.empr_id = {empr_id} and cot1.cotr_id = {cotr_id} and u1.unit_codigo = {unit_codigo} and e1.ucap_id = 1 and e1.elem_id = {elem_id}
                          order by cotm1.aap_id"""
          ).on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> unit_codigo,
              'elem_id -> elem_id
            )
            .as(
              SqlParser.scalar[Int] *
            )
          val _qmat = """select e1.elem_descripcion, cotm1.cotrma_unidad AS elpr_unidad, cotm1.cotrma_valor_unitario AS elpr_precio, SUM(cotm1.cotrma_cantidad) as even_cantidad FROM siap.cobro_orden_trabajo cot1
                         inner join siap.cobro_orden_trabajo_material cotm1 ON cotm1.cotr_id = cot1.cotr_id
                         inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                         inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                         where cot1.empr_id = {empr_id} AND cot1.cotr_id = {cotr_id} and u1.unit_codigo = {unit_codigo} AND e1.elem_estado = 1 AND CAST(cotm1.aap_id as VARCHAR) IN ({luminarias})
                         group by 1,2,3
                         order by 2 DESC"""
          SQL(_qmat)
            .on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> unit_codigo,
              'luminarias -> _luminarias.mkString(",").split(",").toSeq
            )
            .as(_parseMaterial *)
        case None =>
          val _qmat = """select e1.elem_descripcion, cotm1.cotrma_unidad AS elpr_unidad, cotm1.cotrma_valor_unitario AS elpr_precio, SUM(cotm1.cotrma_cantidad) AS even_cantidad from siap.cobro_orden_trabajo_material cotm1
                         inner join siap.elemento e1 on e1.elem_id = cotm1.elem_id
                         inner join siap.unitario u1 on u1.unit_id = cotm1.unit_id
                         where cotm1.cotr_id = {cotr_id} and u1.unit_codigo = {unit_codigo} AND e1.elem_estado = 1
                         group by 1,2,3
                         order by 2 DESC"""
          SQL(
            _qmat
          ).on(
              'cotr_id -> orden.cotr_id,
              'empr_id -> empresa.empr_id.get,
              'unit_codigo -> unit_codigo
            )
            .as(_parseMaterial *)
      }    
      val _cantidad = SQL(
        """select count(*) from siap.cobro_orden_trabajo_reporte cotr1
                              where cotr1.cotr_id = {cotr_id}"""
      ).on('cotr_id -> orden.cotr_id).as(scalar[Int].single)

      var _total=0D
      var _cantidad103 = 0.0
      _materiales.map { _m =>
        _m._3 match {
          case Some(precio) =>
            _cantidad103 = _m._4
            val _cant = unit_codigo match {
                          case "1.03" =>
                            _m._4
                          case _ =>
                            _m._4 / _cantidad_xls
                        }
            _total = _total + (precio * _cant)
          case None =>
            _total += 0
        }
      }
      (_total, _cantidad103)
    }
  }

  def util_cobro_orden_trabajo_mano_obra(empresa: Empresa, orden: orden_trabajo_cobro, unit_codigo: String, elem_id: Option[Int], _cantidad103: Double) = {
    db.withConnection{ implicit connection =>
      val _parseManoObra = int("maob_id") ~ str("maob_descripcion") ~ get[Option[Int]](
        "maobpr_precio"
      ) ~ double("maobpr_rendimiento") map { case a0 ~ a1 ~ a2 ~ a3 => (a0, a1, a2, a3) }
      val _manoObra = db.withConnection { implicit connection =>
        SQL(
          """SELECT mob1.maob_id, mob1.maob_descripcion, mop1.maobpr_precio, mop1.maobpr_rendimiento from siap.mano_obra mob1
                  INNER JOIN siap.mano_obra_precio mop1 ON mop1.maob_id = mob1.maob_id
                  WHERE mop1.maobpr_anho = {anho} and mob1.empr_id = {empr_id} and 
                  mop1.cotr_tipo_obra = {cotr_tipo_obra} and mop1.cotr_tipo_obra_tipo = {cotr_tipo_obra_tipo}
                  order by mob1.maob_id"""
        ).on(
          'anho -> orden.cotr_anho.get,
          'empr_id -> empresa.empr_id,
          'cotr_tipo_obra -> orden.cotr_tipo_obra.get,
          'cotr_tipo_obra_tipo -> orden.cotr_tipo_obra_tipo.get
        )
        .as(_parseManoObra *)
      }
      var _total=0D
      _manoObra.map { _o =>
        if (orden.cotr_tipo_obra.get == 2) {
          if (!((_o._1 == 4 || _o._1 == 5 || _o._1 == 6) && (unit_codigo == "1.03" || unit_codigo == "1.04"))) {
            _o._3 match {
              case Some(precio) =>
                var _rendimiento = unit_codigo match {
                  case "1.03" => _o._1 match {
                                  case 1 => (480/_cantidad103) * 3
                                  case _ => 480/_cantidad103
                                }
                  case _ => _o._4
                }
                // println("mano obra unitario: " + unit_codigo + ": cod mano obra" + _o._1 + ": v.unitario" + precio + ": rendimiento:" + _rendimiento)
                _total += (1 * precio) / _rendimiento
              case None =>
                _total += 0
            }
          }
        } else {
            _o._3 match {
              case Some(precio) =>
                var _rendimiento = unit_codigo match {
                  case "1.03" => _o._1 match {
                                  case 1 => (480/_cantidad103) * 3
                                  case _ => 480/_cantidad103
                                }
                  case _ => _o._4
                }
                // println("mano obra unitario: " + unit_codigo + ": cod mano obra" + _o._1 + ": v.unitario" + precio + ": rendimiento:" + _rendimiento)
                _total += (1 * precio) / _rendimiento
              case None =>
                _total += 0
            }          
        }
      }
      // println("mano obra calculado: " + orden.cotr_consecutivo + ":" + _total)
      _total        
    }  
  }

  def util_cobro_orden_trabajo_mano_ingenieria(empresa: Empresa, orden: orden_trabajo_cobro, _subtotal: Double) = {
    db.withConnection { implicit connection =>
        val _ingParser = str("main_descripcion") ~ double("mainpr_precio") map {
          case main_descripcion ~ mainpr_precio =>
            (main_descripcion, mainpr_precio)
        }
        var _ingLista = db.withConnection {
          implicit connection =>
            SQL("""select main_descripcion, mainpr_precio from siap.mano_ingenieria mi1
                left join siap.mano_ingenieria_precio mip1 on mip1.main_id = mi1.main_id 
                where mip1.mainpr_anho = {anho} and mi1.empr_id = {empr_id}""")
              .on(
                'anho -> orden.cotr_anho.get,
                'empr_id -> empresa.empr_id
              )
              .as(_ingParser.*)
        }  
      var _total=0D
      _ingLista.map { _i =>
          _total += (_subtotal * _i._2)
      }
      // println("mano ingenieria calculado: " + orden.cotr_consecutivo + ":" + _total)
      _total
    }
  }
}
