package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream, FileInputStream}
import java.util.{Map, HashMap, Date}
import java.lang.Long
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
//

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, int, str, scalar}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.collection.mutable.Map
import scala.collection.mutable.ListBuffer

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
import org.checkerframework.checker.units.qual.s
import org.apache.poi.ss.usermodel.CellType

case class InformeCambios(
  ortr_consecutivo: Option[scala.Long],
  cuad_descripcion: Option[String],
  reti_descripcion: Option[String],
  repo_consecutivo: Option[scala.Long],
  repo_fecharecepcion: Option[DateTime],
  repo_fechasolucion: Option[DateTime],
  aap_id: Option[scala.Long],
  even_direccion_anterior: Option[String],
  even_direccion: Option[String],
  barr_id_anterior: Option[String],
  barr_id: Option[String],
  aap_apoyo_anterior: Option[String],
  aap_apoyo: Option[String],
  aaco_id_anterior: Option[String],
  aaco_id: Option[String],
  aap_tecnologia_anterior: Option[String],
  aap_tecnologia: Option[String],
  aap_potencia_anterior: Option[String],
  aap_potencia: Option[String],
  aatc_id_anterior: Option[String],
  aatc_id: Option[String],
  aama_id_anterior: Option[String],
  aama_id: Option[String],
  aamo_id_anterior: Option[String],
  aamo_id: Option[String],
  aacu_id_anterior: Option[String],
  aacu_id: Option[String],
  aaus_id_anterior: Option[String],
  aaus_id: Option[String],
  medi_id_anterior: Option[String],
  medi_id: Option[String],
  tran_id_anterior: Option[String],
  tran_id: Option[String],
  aap_lat_anterior: Option[String],
  aap_lat: Option[String],
  aap_lng_anterior: Option[String],
  aap_lng: Option[String],
  aap_brazo_anterior: Option[String],
  aap_brazo: Option[String],
  aap_collarin_anterior: Option[String],
  aap_collarin: Option[String],
  aap_poste_propietario_anterior: Option[String],
  aap_poste_propietario: Option[String],
  aap_poste_altura_anterior: Option[String],
  aap_poste_altura: Option[String],
  tipo_id_anterior: Option[String],
  tipo_id: Option[String]
)

object InformeCambios {
  val _set = { 
                  get[Option[scala.Long]]("ortr_consecutivo") ~
                   get[Option[String]]("cuad_descripcion") ~
                   get[Option[String]]("reti_descripcion") ~
                   get[Option[scala.Long]]("repo_consecutivo") ~
                   get[Option[DateTime]]("repo_fecharecepcion") ~
                   get[Option[DateTime]]("repo_fechasolucion") ~                   
                   get[Option[scala.Long]]("aap_id") ~
                   get[Option[String]]("even_direccion_anterior") ~
                   get[Option[String]]("even_direccion") ~
                   get[Option[String]]("barr_id_anterior") ~
                   get[Option[String]]("barr_id") ~
                   get[Option[String]]("aap_apoyo_anterior") ~
                   get[Option[String]]("aap_apoyo") ~
                   get[Option[String]]("aaco_id_anterior") ~
                   get[Option[String]]("aaco_id") ~
                   get[Option[String]]("aap_tecnologia_anterior") ~
                   get[Option[String]]("aap_tecnologia") ~
                   get[Option[String]]("aap_potencia_anterior") ~
                   get[Option[String]]("aap_potencia") ~
                   get[Option[String]]("aatc_id_anterior") ~
                   get[Option[String]]("aatc_id") ~
                   get[Option[String]]("aama_id_anterior") ~
                   get[Option[String]]("aama_id") ~
                   get[Option[String]]("aamo_id_anterior") ~
                   get[Option[String]]("aamo_id") ~
                   get[Option[String]]("aacu_id_anterior") ~
                   get[Option[String]]("aacu_id") ~
                   get[Option[String]]("aaus_id_anterior") ~
                   get[Option[String]]("aaus_id") ~
                   get[Option[String]]("medi_id_anterior") ~
                   get[Option[String]]("medi_id") ~
                   get[Option[String]]("tran_id_anterior") ~
                   get[Option[String]]("tran_id") ~
                   get[Option[String]]("aap_lat_anterior") ~
                   get[Option[String]]("aap_lat") ~
                   get[Option[String]]("aap_lng_anterior") ~
                   get[Option[String]]("aap_lng") ~
                   get[Option[String]]("aap_brazo_anterior") ~
                   get[Option[String]]("aap_brazo") ~
                   get[Option[String]]("aap_collarin_anterior") ~
                   get[Option[String]]("aap_collarin") ~
                   get[Option[String]]("aap_poste_propietario_anterior") ~
                   get[Option[String]]("aap_poste_propietario") ~
                   get[Option[String]]("aap_poste_altura_anterior") ~
                   get[Option[String]]("aap_poste_altura") ~
                   get[Option[String]]("tipo_id_anterior") ~
                   get[Option[String]]("tipo_id") map {
                    case a01 ~ a02 ~ a03 ~ a04 ~ a05 ~ a06 ~ a07 ~ a08 ~ a09 ~ a10 ~ 
                         a11 ~ a12 ~ a13 ~ a14 ~ a15 ~ a16 ~ a17 ~ a18 ~ a19 ~ a20 ~ 
                         a21 ~ a22 ~ a23 ~ a24 ~ a25 ~ a26 ~ a27 ~ a28 ~ a29 ~ a30 ~ 
                         a31 ~ a32 ~ a33 ~ a34 ~ a35 ~ a36 ~ a37 ~ a38 ~ a39 ~ a40 ~ 
                         a41 ~ a42 ~ a43 ~ a44 ~ a45 ~ a46 ~ a47 => new InformeCambios(
                           a01, a02, a03, a04, a05, a06, a07, a08, a09, a10,
                           a11, a12, a13, a14, a15, a16, a17, a18, a19, a20,
                           a21, a22, a23, a24, a25, a26, a27, a28, a29, a30,
                           a31, a32, a33, a34, a35, a36, a37, a38, a39, a40,
                           a41, a42, a43, a44, a45, a46, a47)
                   }
        }
}

case class OrdenEvento(
    even_id: Option[Int],
    even_estado: Option[Int],
    repo_id: Option[scala.Long],
    reti_id: Option[scala.Long],
    repo_consecutivo: Option[Int],
    repo_descripcion: Option[String],
    tireuc_id: Option[scala.Long]
)
case class OrdenObra(
    even_id: Option[Int],
    even_estado: Option[Int],
    obra_id: Option[scala.Long],
    obra_consecutivo: Option[Int],
    obra_nombre: Option[String]
)
case class OrdenNovedad(
    nove_id: Option[Int],
    ortrno_horaini: Option[String],
    ortrno_horafin: Option[String],
    ortrno_observacion: Option[String],
    even_id: Option[Int],
    even_estado: Option[Int]
)

case class OrdenTrabajo(
    ortr_id: Option[scala.Long],
    ortr_fecha: Option[DateTime],
    ortr_observacion: Option[String],
    ortr_consecutivo: Option[Int],
    otes_id: Option[scala.Long],
    cuad_id: Option[scala.Long],
    cuad_descripcion: Option[String],
    tiba_id: Option[scala.Long],
    empr_id: Option[scala.Long],
    usua_id: Option[scala.Long],
    reportes: Option[List[OrdenEvento]],
    obras: Option[List[OrdenObra]],
    novedades: Option[List[OrdenNovedad]]
)

object OrdenEvento {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val ordeneventoWrites = new Writes[OrdenEvento] {
    def writes(ortr: OrdenEvento) = Json.obj(
      "even_id" -> ortr.even_id,
      "even_estado" -> ortr.even_estado,
      "repo_id" -> ortr.repo_id,
      "reti_id" -> ortr.reti_id,
      "repo_consecutivo" -> ortr.repo_consecutivo,
      "repo_descripcion" -> ortr.repo_descripcion,
      "tireuc_id" -> ortr.tireuc_id
    )
  }

  implicit val ordeneventoReads: Reads[OrdenEvento] = (
    (__ \ "even_id").readNullable[Int] and
      (__ \ "even_estado").readNullable[Int] and
      (__ \ "repo_id").readNullable[scala.Long] and
      (__ \ "reti_id").readNullable[scala.Long] and
      (__ \ "repo_consecutivo").readNullable[Int] and
      (__ \ "repo_descripcion").readNullable[String] and
      (__ \ "tireuc_id").readNullable[scala.Long]
  )(OrdenEvento.apply _)

  val set = {
    get[Option[Int]]("even_id") ~
      get[Option[Int]]("even_estado") ~
      get[Option[scala.Long]]("repo_id") ~
      get[Option[scala.Long]]("reti_id") ~
      get[Option[Int]]("repo_consecutivo") ~
      get[Option[String]]("repo_descripcion") ~
      get[Option[scala.Long]]("tireuc_id") map {
      case even_id ~ even_estado ~ repo_id ~ reti_id ~ repo_consecutivo ~ repo_descripcion ~ tireuc_id =>
        OrdenEvento(
          even_id,
          even_estado,
          repo_id,
          reti_id,
          repo_consecutivo,
          repo_descripcion,
          tireuc_id
        )
    }
  }
}

object OrdenObra {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val ordenobraWrites = new Writes[OrdenObra] {
    def writes(ortr: OrdenObra) = Json.obj(
      "even_id" -> ortr.even_id,
      "even_estado" -> ortr.even_estado,
      "obra_id" -> ortr.obra_id,
      "obra_consecutivo" -> ortr.obra_consecutivo,
      "obra_nombre" -> ortr.obra_nombre
    )
  }

  implicit val ordenobraReads: Reads[OrdenObra] = (
    (__ \ "even_id").readNullable[Int] and
      (__ \ "even_estado").readNullable[Int] and
      (__ \ "obra_id").readNullable[scala.Long] and
      (__ \ "obra_consecutivo").readNullable[Int] and
      (__ \ "obra_nombre").readNullable[String]
  )(OrdenObra.apply _)

  val set = {
    get[Option[Int]]("even_id") ~
      get[Option[Int]]("even_estado") ~
      get[Option[scala.Long]]("obra_id") ~
      get[Option[Int]]("obra_consecutivo") ~
      get[Option[String]]("obra_nombre") map {
      case even_id ~ even_estado ~ obra_id ~ obra_consecutivo ~ obra_nombre =>
        OrdenObra(even_id, even_estado, obra_id, obra_consecutivo, obra_nombre)
    }
  }
}

object OrdenNovedad {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val wWrites = new Writes[OrdenNovedad] {
    def writes(ortr: OrdenNovedad) = Json.obj(
      "nove_id" -> ortr.nove_id,
      "ortrno_horaini" -> ortr.ortrno_horaini,
      "ortrno_horafin" -> ortr.ortrno_horafin,
      "ortrno_observacion" -> ortr.ortrno_observacion,
      "even_id" -> ortr.even_id,
      "even_estado" -> ortr.even_estado
    )
  }

  implicit val ordenobraReads: Reads[OrdenNovedad] = (
    (__ \ "nove_id").readNullable[Int] and
      (__ \ "ortrno_horaini").readNullable[String] and
      (__ \ "ortrno_horafin").readNullable[String] and
      (__ \ "ortrno_observacion").readNullable[String] and
      (__ \ "even_id").readNullable[Int] and
      (__ \ "even_estado").readNullable[Int]
  )(OrdenNovedad.apply _)

  val set = {
    get[Option[Int]]("nove_id") ~
      get[Option[String]]("ortrno_horaini") ~
      get[Option[String]]("ortrno_horafin") ~
      get[Option[String]]("ortrno_observacion") ~
      get[Option[Int]]("even_id") ~
      get[Option[Int]]("even_estado") map {
      case nove_id ~ ortrno_horaini ~ ortrno_horafin ~ ortrno_observacion ~ even_id ~ even_estado =>
        OrdenNovedad(
          nove_id,
          ortrno_horaini,
          ortrno_horafin,
          ortrno_observacion,
          even_id,
          even_estado
        )
    }
  }
}

object OrdenTrabajo {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val ordentrabajoWrites = new Writes[OrdenTrabajo] {
    def writes(ortr: OrdenTrabajo) = Json.obj(
      "ortr_id" -> ortr.ortr_id,
      "ortr_fecha" -> ortr.ortr_fecha,
      "ortr_observacion" -> ortr.ortr_observacion,
      "ortr_consecutivo" -> ortr.ortr_consecutivo,
      "otes_id" -> ortr.otes_id,
      "cuad_id" -> ortr.cuad_id,
      "cuad_descripcion" -> ortr.cuad_descripcion,
      "tiba_id" -> ortr.tiba_id,
      "empr_id" -> ortr.empr_id,
      "usua_id" -> ortr.usua_id,
      "reportes" -> ortr.reportes,
      "obras" -> ortr.obras,
      "novedades" -> ortr.novedades
    )
  }

  implicit val ordentrabajoReads: Reads[OrdenTrabajo] = (
    (__ \ "ortr_id").readNullable[scala.Long] and
      (__ \ "ortr_fecha").readNullable[DateTime] and
      (__ \ "ortr_observacion").readNullable[String] and
      (__ \ "ortr_consecutivo").readNullable[Int] and
      (__ \ "otes_id").readNullable[scala.Long] and
      (__ \ "cuad_id").readNullable[scala.Long] and
      (__ \ "cuad_descripcion").readNullable[String] and
      (__ \ "tiba_id").readNullable[scala.Long] and
      (__ \ "empr_id").readNullable[scala.Long] and
      (__ \ "usua_id").readNullable[scala.Long] and
      (__ \ "reportes").readNullable[List[OrdenEvento]] and
      (__ \ "obras").readNullable[List[OrdenObra]] and
      (__ \ "novedades").readNullable[List[OrdenNovedad]]
  )(OrdenTrabajo.apply _)

  /**
    * Parsear un OrdenTrabajo desde un ResultSet
    */
  val simple = {
    get[Option[scala.Long]]("ortr_id") ~
      get[Option[DateTime]]("ortr_fecha") ~
      get[Option[String]]("ortr_observacion") ~
      get[Option[Int]]("ortr_consecutivo") ~
      get[Option[scala.Long]]("otes_id") ~
      get[Option[scala.Long]]("cuad_id") ~
      get[Option[String]]("cuad_descripcion") ~
      get[Option[scala.Long]]("ordentrabajo.tiba_id") ~
      get[Option[scala.Long]]("ordentrabajo.empr_id") ~
      get[Option[scala.Long]]("ordentrabajo.usua_id") map {
      case ortr_id ~ ortr_fecha ~ ortr_observacion ~ ortr_consecutivo ~ otes_id ~ cuad_id ~ cuad_descripcion ~ tiba_id ~ empr_id ~ usua_id =>
        OrdenTrabajo(
          ortr_id,
          ortr_fecha,
          ortr_observacion,
          ortr_consecutivo,
          otes_id,
          cuad_id,
          cuad_descripcion,
          tiba_id,
          empr_id,
          usua_id,
          None,
          None,
          None
        )
    }
  }
}

class OrdenTrabajoRepository @Inject()(
    dbapi: DBApi,
    empresaService: EmpresaRepository,
    reporteService: ReporteRepository,
    controlReporteService: ControlReporteRepository,
    transformadorReporteService: TransformadorReporteRepository,
    obraService: ObraRepository
)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")
  private val REPORT_DEFINITION_PATH = System.getProperty("user.dir") + "/conf/reports/"

  /**
    * Recuperar un OrdenTrabajo por su ortr_id
    * @param ortr_id: scala.Long
    */
  def buscarPorId(ortr_id: scala.Long): Option[OrdenTrabajo] = {
    db.withConnection { implicit connection =>
      val o = SQL("""SELECT * FROM siap.ordentrabajo ot
                           LEFT JOIN siap.cuadrilla c1 ON c1.cuad_id = ot.cuad_id 
                           WHERE ortr_id = {ortr_id}""")
        .on(
          'ortr_id -> ortr_id
        )
        .as(OrdenTrabajo.simple.single)
      val e = SQL(
        """SELECT e.even_id, e.even_estado, e.repo_id, r.reti_id, r.repo_consecutivo, r.repo_descripcion, e.tireuc_id FROM siap.ordentrabajo_reporte e 
                           LEFT JOIN siap.reporte r ON r.repo_id = e.repo_id
                           WHERE e.ortr_id = {ortr_id}"""
      ).on(
          'ortr_id -> ortr_id
        )
        .as(OrdenEvento.set *)
      val b = SQL(
        """SELECT e.even_id, e.even_estado, e.obra_id, r.obra_consecutivo, r.obra_nombre FROM siap.ordentrabajo_obra e 
                           LEFT JOIN siap.obra r ON r.obra_id = e.obra_id
                           WHERE e.ortr_id = {ortr_id}"""
      ).on(
          'ortr_id -> ortr_id
        )
        .as(OrdenObra.set *)
      val c = SQL(
        """SELECT n.nove_id, n.ortrno_horaini, n.ortrno_horafin, n.ortrno_observacion, n.even_id, n.even_estado FROM siap.ordentrabajo_novedad n 
                           WHERE n.ortr_id = {ortr_id}"""
      ).on(
          'ortr_id -> ortr_id
        )
        .as(OrdenNovedad.set *)
      Some(o.copy(reportes = Some(e), obras = Some(b), novedades = Some(c)))
    }
  }

  /**
    * Recuperar un OrdenTrabajo por su ortr_consecutivo
    * @param ortr_consecutivo: Int
    * @param empr_id: scala.Long
    */
  def buscarPorConsecutivo(
      ortr_consecutivo: Int,
      empr_id: scala.Long
  ): Option[OrdenTrabajo] = {
    db.withConnection { implicit connection =>
      SQL(
        """SELECT * FROM siap.ordentrabajo ot
                   LEFT JOIN siap.cuadrilla c1 ON c1.cuad_id = ot.cuad_id
                   WHERE ortr_consecutivo = {ortr_consecutivo} and ot.empr_id = {empr_id}"""
      ).on(
          'ortr_consecutivo -> ortr_consecutivo,
          'empr_id -> empr_id
        )
        .as(OrdenTrabajo.simple.singleOpt)
    }
  }

  /**
    * Recuperar el último OrdenTrabajo por su empr_id
    * @param empr_id: scala.Long
    */
  def buscarUltimoPorEmpresa(empr_id: scala.Long): Option[OrdenTrabajo] = {
    db.withConnection { implicit connection =>
      SQL(
        """SELECT * FROM siap.ordentrabajo ot 
              LEFT JOIN siap.cuadrilla c1 ON c1.cuad_id = ot.cuad_id
              WHERE ot.empr_id = {empr_id} ORDER BY ot.ortr_id DESC LIMIT 1 OFFSET 0"""
      ).on(
          'empr_id -> empr_id
        )
        .as(OrdenTrabajo.simple.singleOpt)
    }
  }

  /**
    * Recuperar total de registros
    * @param empr_id: scala.Long
    * @return total
    */
  def cuenta(empr_id: scala.Long): scala.Long = {
    db.withConnection { implicit connection =>
      val result = SQL(
        "SELECT COUNT(*) AS c FROM siap.ordentrabajo ot WHERE ot.empr_id = {empr_id} and ot.otes_id <> 99"
      ).on(
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[scala.Long].single)
      result
    }
  }

  /**
    Recuperar todos los Elemento de una empresa
    @param empr_id: scala.Long
    @param page_size: scala.Long
    @param current_page: scala.Long
    @return Future[Iterable[Elemento]]
    */
  def todos(
      page_size: scala.Long,
      current_page: scala.Long,
      empr_id: scala.Long,
      orderby: String,
      filter: String
  ): Future[Iterable[OrdenTrabajo]] =
    Future[Iterable[OrdenTrabajo]] {
      db.withConnection { implicit connection =>
        var _list: ListBuffer[OrdenTrabajo] = new ListBuffer[OrdenTrabajo]()
        SQL(
          """SELECT * FROM siap.ordentrabajo ot
            LEFT JOIN siap.cuadrilla c1 ON c1.cuad_id = ot.cuad_id
            WHERE ot.empr_id = {empr_id} AND ot.otes_id <> 99 ORDER BY ot.ortr_id DESC LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1) """
        ).on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page
          )
          .as(OrdenTrabajo.simple *)
      }
    }

  /**
    * Recuperar todos los OrdenTrabajo de una Empresa usando el empr_id
    * @param empr_id: scala.Long
    */
  def ordenes(empr_id: scala.Long): Future[Iterable[OrdenTrabajo]] =
    Future[Iterable[OrdenTrabajo]] {
      db.withConnection { implicit connection =>
        SQL(
          """SELECT * FROM siap.ordentrabajo ot
                   LEFT JOIN siap.cuadrilla c1 ON c1.cuad_id = ot.cuad_id
                   WHERE ot.empr_id = {empr_id} AND ot.otes_id <> 99 ORDER BY ot.ortr_id DESC"""
        ).on(
            'empr_id -> empr_id
          )
          .as(OrdenTrabajo.simple *)
      }
    }

  /**
    * Crear OrdenTrabajo
    * @param ortr: OrdenTrabajo
    */
  def crear(
      ortr: OrdenTrabajo,
      empr_id: scala.Long,
      usua_id: scala.Long
  ): Future[scala.Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      var consecutivo = SQL(
        """SELECT COUNT(*) FROM siap.ordentrabajo ot WHERE ot.empr_id = {empr_id}"""
      ).on(
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[Int].single)
      consecutivo = consecutivo + 1
      val id: scala.Long = SQL(
        """INSERT INTO siap.ordentrabajo (ortr_fecha, ortr_observacion, ortr_consecutivo, otes_id, cuad_id, empr_id, usua_id, tiba_id) VALUES ({ortr_fecha}, {ortr_observacion}, {ortr_consecutivo}, {otes_id}, {cuad_id}, {empr_id}, {usua_id}, {tiba_id})"""
      ).on(
          'ortr_fecha -> ortr.ortr_fecha,
          'ortr_observacion -> ortr.ortr_observacion,
          'ortr_consecutivo -> consecutivo,
          'otes_id -> ortr.otes_id,
          'cuad_id -> ortr.cuad_id,
          'empr_id -> empr_id,
          'usua_id -> usua_id,
          'tiba_id -> ortr.tiba_id
        )
        .executeInsert()
        .get
      // Guardar relacion de reportes
      ortr.reportes.map { reportes =>
        for (r <- reportes) {
          r.repo_consecutivo match {
            case Some(consec) =>
              val reporte = r.tireuc_id match { 
                case Some(1) => reporteService.buscarPorId(r.repo_id.get)
                case Some(2) => controlReporteService.buscarPorId(r.repo_id.get)
                case Some(3) => transformadorReporteService.buscarPorId(r.repo_id.get)
              }
              reporte match {
                case Some(rep) =>
                  if (rep.rees_id.get < 3) {
                    SQL(
                      """INSERT INTO siap.ordentrabajo_reporte (ortr_id, repo_id, even_id, even_estado, tireuc_id) VALUES ({ortr_id}, {repo_id}, {even_id}, {even_estado}, {tireuc_id})"""
                    ).on(
                      'ortr_id -> id,
                      'repo_id -> r.repo_id,
                      'even_id -> r.even_id,
                      'even_estado -> r.even_estado,
                      'tireuc_id -> r.tireuc_id
                    )
                    .executeInsert()
                    SQL("""UPDATE siap.reporte SET repo_fechasolucion = {repo_fechasolucion} where tireuc_id = {tireuc_id} and repo_id = {repo_id}""").
                    on(
                      'repo_fechasolucion -> Option.empty[DateTime],
                      'tireuc_id -> r.tireuc_id,
                      'repo_id -> r.repo_id
                    ).executeUpdate()
                  }
                case None => None
              }
            case None => None
          }
        }
      }

      // Guardar relacion de obras
      ortr.obras.map { reportes =>
        for (r <- reportes) {
          r.obra_consecutivo match {
            case Some(consec) =>
              SQL(
                """INSERT INTO siap.ordentrabajo_obra (ortr_id, obra_id, even_id, even_estado) VALUES ({ortr_id}, {obra_id}, {even_id}, {even_estado})"""
              ).on(
                  'ortr_id -> id,
                  'obra_id -> r.obra_id,
                  'even_id -> r.even_id,
                  'even_estado -> r.even_estado
                )
                .executeInsert()
            case None => None
          }
        }
      }

      // Guardar Novedades
      ortr.novedades.map { novedades =>
        for (n <- novedades) {
          n.nove_id match {
            case Some(nove_id) =>
              SQL(
                """INSERT INTO siap.ordentrabajo_novedad (ortr_id, nove_id, ortrno_horaini, ortrno_horafin, ortrno_observacion, even_id, even_estado) VALUES ({ortr_id}, {nove_id}, {ortrno_horaini}, {ortrno_horafin}, {ortrno_observacion}, {even_id}, {even_estado})"""
              ).on(
                  'ortr_id -> id,
                  'nove_id -> n.nove_id,
                  'ortrno_horaini -> n.ortrno_horaini,
                  'ortrno_horafin -> n.ortrno_horafin,
                  'ortrno_observacion -> n.ortrno_observacion,
                  'even_id -> n.even_id,
                  'even_estado -> n.even_estado
                )
                .executeInsert()
            case None => None
          }
        }
      }

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> ortr.usua_id,
          'audi_tabla -> "ordentrabajo",
          'audi_uid -> id,
          'audi_campo -> "ortr_consecutivo",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> ortr.ortr_consecutivo,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
    *  Actualizar OrdenTrabajo
    *  @param ortr: OrdenTrabajo
    */
  def actualizar(ortr: OrdenTrabajo): Boolean = {
    val ortr_ant: Option[OrdenTrabajo] = buscarPorId(ortr.ortr_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val count: scala.Long = SQL(
        "UPDATE siap.ordentrabajo SET ortr_fecha = {ortr_fecha}, ortr_observacion = {ortr_observacion}, ortr_consecutivo = {ortr_consecutivo}, otes_id = {otes_id}, cuad_id = {cuad_id}, empr_id = {empr_id}, usua_id = {usua_id} WHERE ortr_id = {ortr_id}"
      ).on(
          "ortr_id" -> ortr.ortr_id,
          "ortr_fecha" -> ortr.ortr_fecha,
          "ortr_observacion" -> ortr.ortr_observacion,
          "ortr_consecutivo" -> ortr.ortr_consecutivo,
          "otes_id" -> ortr.otes_id,
          "cuad_id" -> ortr.cuad_id,
          "empr_id" -> ortr.empr_id,
          "usua_id" -> ortr.usua_id
        )
        .executeUpdate()

      // Guardar relacion de reportes
      ortr.reportes.map { reportes =>
        for (r <- reportes) {
          r.repo_consecutivo match {
            case Some(consec) =>
              val reporte = r.tireuc_id match { 
                case Some(1) => reporteService.buscarPorId(r.repo_id.get)
                case Some(2) => controlReporteService.buscarPorId(r.repo_id.get)
                case Some(3) => transformadorReporteService.buscarPorId(r.repo_id.get)
              }
              reporte match {
                case Some(rep) =>
                  if (rep.rees_id.get < 3) {
                    val esactualizado: Boolean = SQL(
                      """UPDATE siap.ordentrabajo_reporte SET repo_id = {repo_id}, even_estado = {even_estado}, tireuc_id = {tireuc_id} where ortr_id = {ortr_id} and even_id = {even_id}"""
                    ).on(
                      'ortr_id -> ortr.ortr_id,
                      'even_id -> r.even_id,
                      'repo_id -> r.repo_id,
                      'even_estado -> r.even_estado,
                      'tireuc_id -> r.tireuc_id
                    )
                   .executeUpdate() > 0
                    if (!esactualizado) {
                      SQL(
                        """INSERT INTO siap.ordentrabajo_reporte (ortr_id, repo_id, even_id, even_estado, tireuc_id) VALUES ({ortr_id}, {repo_id}, {even_id}, {even_estado}, {tireuc_id})"""
                      ).on(
                       'ortr_id -> ortr.ortr_id,
                       'repo_id -> r.repo_id,
                       'even_id -> r.even_id,
                        'even_estado -> r.even_estado,
                       'tireuc_id -> r.tireuc_id
                     )
                      .executeInsert()
                    }
                    SQL("""UPDATE siap.reporte SET repo_fechasolucion = {repo_fechasolucion} where tireuc_id = {tireuc_id} and repo_id = {repo_id}""").
                    on(
                     'repo_fechasolucion -> Option.empty[DateTime],
                     'tireuc_id -> r.tireuc_id,
                    'repo_id -> r.repo_id
                    ).executeUpdate()
                  }
                case None => None
              }
            case None => None
          }
        }
      }

      // Guardar relacion de reportes
      ortr.obras.map { reportes =>
        for (r <- reportes) {
          r.obra_consecutivo match {
            case Some(consec) =>
              val esactualizado: Boolean = SQL(
                """UPDATE siap.ordentrabajo_obra SET obra_id = {obra_id}, even_estado = {even_estado} where ortr_id = {ortr_id} and even_id = {even_id}"""
              ).on(
                  'ortr_id -> ortr.ortr_id,
                  'even_id -> r.even_id,
                  'obra_id -> r.obra_id,
                  'even_estado -> r.even_estado
                )
                .executeUpdate() > 0
              if (!esactualizado) {
                SQL(
                  """INSERT INTO siap.ordentrabajo_obra (ortr_id, obra_id, even_id, even_estado) VALUES ({ortr_id}, {obra_id}, {even_id}, {even_estado})"""
                ).on(
                    'ortr_id -> ortr.ortr_id,
                    'obra_id -> r.obra_id,
                    'even_id -> r.even_id,
                    'even_estado -> r.even_estado
                  )
                  .executeInsert()
              }
            case None => None
          }
        }
      }

      // Guardar relacion de novedades
      ortr.novedades.map { novedades =>
        for (n <- novedades) {
          n.even_id match {
            case Some(even_id) =>
              val esactualizado: Boolean = SQL(
                """UPDATE siap.ordentrabajo_novedad SET nove_id = {nove_id}, ortrno_horaini = {ortrno_horaini}, ortrno_horafin = {ortrno_horafin}, ortrno_observacion = {ortrno_observacion}, even_estado = {even_estado} where ortr_id = {ortr_id} and even_id = {even_id}"""
              ).on(
                  'ortr_id -> ortr.ortr_id,
                  'even_id -> n.even_id,
                  'nove_id -> n.nove_id,
                  'ortrno_horaini -> n.ortrno_horaini,
                  'ortrno_horafin -> n.ortrno_horafin,
                  'ortrno_observacion -> n.ortrno_observacion,
                  'even_estado -> n.even_estado
                )
                .executeUpdate() > 0
              if (!esactualizado) {
                SQL(
                  """INSERT INTO siap.ordentrabajo_novedad (ortr_id, nove_id, ortrno_horaini, ortrno_horafin, ortrno_observacion, even_id, even_estado) VALUES ({ortr_id}, {nove_id}, {ortrno_horaini}, {ortrno_horafin}, {ortrno_observacion}, {even_id}, {even_estado})"""
                ).on(
                    'ortr_id -> ortr.ortr_id,
                    'even_id -> n.even_id,
                    'nove_id -> n.nove_id,
                    'ortrno_horaini -> n.ortrno_horaini,
                    'ortrno_horafin -> n.ortrno_horafin,
                    'ortrno_observacion -> n.ortrno_observacion,
                    'even_estado -> n.even_estado
                  )
                  .executeInsert()
              }
            case None => None
          }
        }
      }

      if (ortr_ant != None) {
        if (ortr_ant.get.ortr_fecha != ortr.ortr_fecha) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> ortr.usua_id,
              'audi_tabla -> "ordentrabajo",
              'audi_uid -> ortr.ortr_id,
              'audi_campo -> "ortr_fecha",
              'audi_valorantiguo -> ortr_ant.get.ortr_fecha,
              'audi_valornuevo -> ortr.ortr_fecha,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (ortr_ant.get.ortr_observacion != ortr.ortr_observacion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> ortr.usua_id,
              'audi_tabla -> "ordentrabajo",
              'audi_uid -> ortr.ortr_id,
              'audi_campo -> "ortr_observacion",
              'audi_valorantiguo -> ortr_ant.get.ortr_observacion,
              'audi_valornuevo -> ortr.ortr_observacion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (ortr_ant.get.ortr_consecutivo != ortr.ortr_consecutivo) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> ortr.usua_id,
              'audi_tabla -> "ordentrabajo",
              'audi_uid -> ortr.ortr_id,
              'audi_campo -> "ortr_consecutivo",
              'audi_valorantiguo -> ortr_ant.get.ortr_consecutivo,
              'audi_valornuevo -> ortr.ortr_consecutivo,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (ortr_ant.get.otes_id != ortr.otes_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> ortr.usua_id,
              'audi_tabla -> "ordentrabajo",
              'audi_uid -> ortr.ortr_id,
              'audi_campo -> "otes_id",
              'audi_valorantiguo -> ortr_ant.get.otes_id,
              'audi_valornuevo -> ortr.otes_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (ortr_ant.get.cuad_id != ortr.cuad_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> ortr.usua_id,
              'audi_tabla -> "ordentrabajo",
              'audi_uid -> ortr.ortr_id,
              'audi_campo -> "cuad_id",
              'audi_valorantiguo -> ortr_ant.get.cuad_id,
              'audi_valornuevo -> ortr.cuad_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (ortr_ant.get.empr_id != ortr.empr_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> ortr.usua_id,
              'audi_tabla -> "ordentrabajo",
              'audi_uid -> ortr.ortr_id,
              'audi_campo -> "empr_id",
              'audi_valorantiguo -> ortr_ant.get.empr_id,
              'audi_valornuevo -> ortr.empr_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (ortr_ant.get.usua_id != ortr.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> ortr.usua_id,
              'audi_tabla -> "ordentrabajo",
              'audi_uid -> ortr.ortr_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> ortr_ant.get.usua_id,
              'audi_valornuevo -> ortr.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
      }

      count > 0
    }
  }

  /**
    * Eliminar Orden Trabajo
    */
  def borrar(ortr_id: scala.Long, usua_id: scala.Long): Boolean = {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())

      val count: scala.Long = SQL(
        "UPDATE siap.ordentrabajo SET otes_id = 9 WHERE ortr_id = {ortr_id}"
      ).on(
          'ortr_id -> ortr_id
        )
        .executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "ordentrabajo",
          'audi_uid -> ortr_id,
          'audi_campo -> "",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> "",
          'audi_evento -> "E"
        )
        .executeInsert()

      count > 0
    }
  }

  /**
    * estado
    */
  def estados(): Future[Iterable[ReporteEstado]] =
    Future[Iterable[ReporteEstado]] {
      db.withConnection { implicit connection =>
        SQL("""SELECT * FROM siap.reporte_estado""").as(ReporteEstado.oEstado *)
      }
    }

  /**
    *  imprimir
    * @param ortr_id: scala.Long
    * @return Array[Byte]
    */
  def imprimir(ortr_id: scala.Long, empr_id: scala.Long): Array[Byte] = {
    var os = Array[Byte]()
    val orden = buscarPorId(ortr_id)
    db.withConnection { implicit connection =>
      empresaService.buscarPorId(empr_id).map { empresa =>
        try {
          var compiledFile = REPORT_DEFINITION_PATH + "siap_orden_trabajo.jasper";
          var reportParams = new HashMap[String, java.lang.Object]()
          reportParams.put("ORTR_ID", new java.lang.Long(ortr_id.longValue()))
          reportParams.put("EMPRESA", empresa.empr_descripcion)
          reportParams.put("DIRECCION", empresa.empr_direccion)
          reportParams.put("CONCESION", empresa.empr_concesion.get)
          os = JasperRunManager
            .runReportToPdf(compiledFile, reportParams, connection)
        } catch {
          case e: Exception => e.printStackTrace();
        }
      }
    }
    os
  }

  /*
   * Agregar reporte
   * */
  def agregarReporte(ortr_id: Long, repo_id: Long, tireuc_id: Int): Boolean = {
    db.withConnection { implicit connection =>
      var result: Boolean = SQL(
        """SELECT COUNT(*) AS total FROM siap.ordentrabajo_reporte WHERE ortr_id = {ortr_id} AND repo_id = {repo_id} AND tireuc_id = {tireuc_id}"""
      ).on(
          'ortr_id -> ortr_id,
          'repo_id -> repo_id,
          'tireuc_id -> tireuc_id
        )
        .as(SqlParser.scalar[Int].single) > 0

      if (!result) {
        var even_id = SQL(
          """SELECT even_id FROM siap.ordentrabajo_reporte WHERE ortr_id = {ortr_id} ORDER BY even_id DESC LIMIT 1"""
        ).on(
            'ortr_id -> ortr_id
          )
          .as(SqlParser.scalar[Int].singleOpt)
        var id = 0
        even_id match {
          case Some(e) => id = e + 1
          case None    => id = 1
        }
        result = SQL(
          """INSERT INTO siap.ordentrabajo_reporte (ortr_id, repo_id, tireuc_id, even_id, even_estado) VALUES ({ortr_id}, {repo_id}, {tireuc_id}, {even_id}, {even_estado})"""
        ).on(
            'ortr_id -> ortr_id,
            'repo_id -> repo_id,
            'tireuc_id -> tireuc_id,
            'even_id -> id,
            'even_estado -> 1
          )
          .executeInsert()
          .get > 0

          SQL("""UPDATE siap.reporte SET repo_fechasolucion = {repo_fechasolucion} where tireuc_id = {tireuc_id} and repo_id = {repo_id}""").
               on(
                 'repo_fechasolucion -> Option.empty[DateTime],
                 'tireuc_id -> tireuc_id,
                 'repo_id -> repo_id
               ).executeUpdate()
      }
      result
    }

  }

  /*
   * Agregar reporte
   * */
  def agregarObra(ortr_id: Long, obra_id: Long): Boolean = {
    db.withConnection { implicit connection =>
      var result: Boolean = SQL(
        """SELECT COUNT(*) AS total FROM siap.ordentrabajo_obra WHERE ortr_id = {ortr_id} AND obra_id = {obra_id}"""
      ).on(
          'ortr_id -> ortr_id,
          'obra_id -> obra_id
        )
        .as(SqlParser.scalar[Int].single) > 0

      if (!result) {
        var even_id = SQL(
          """SELECT even_id FROM siap.ordentrabajo_obra WHERE ortr_id = {ortr_id} ORDER BY even_id DESC LIMIT 1"""
        ).on(
            'ortr_id -> ortr_id
          )
          .as(SqlParser.scalar[Int].singleOpt)
        var id = 0
        even_id match {
          case Some(e) => id = e + 1
          case None    => id = 1
        }
        result = SQL(
          """INSERT INTO siap.ordentrabajo_obra (ortr_id, obra_id, even_id, even_estado) VALUES ({ortr_id}, {obra_id}, {even_id}, {even_estado})"""
        ).on(
            'ortr_id -> ortr_id,
            'obra_id -> obra_id,
            'even_id -> id,
            'even_estado -> 1
          )
          .executeInsert()
          .get > 0
      }
      result
    }

  }

  def obtenerReporteObraPorCuadrilla(
      cuad_id: Long
  ): Iterable[(Int, Int, Int, Int)] = {
    val fecha_corte = new DateTime().toString("yyyy-MM-dd")
    db.withConnection { implicit connection =>
      val _parser = int("ortr_id") ~ int("repo_id") ~ int("reti_id") ~ int(
        "repo_consecutivo"
      ) map {
        case a1 ~ a2 ~ a3 ~ a4 => (a1, a2, a3, a4)
      }
      SQL(
        """select ot1.ortr_id, r1.repo_id, r1.reti_id, r1.repo_consecutivo from siap.ordentrabajo ot1
                    inner join siap.cuadrilla c1 on c1.cuad_id = ot1.cuad_id 
                    inner join siap.ordentrabajo_reporte or1 on or1.ortr_id = ot1.ortr_id 
                    inner join siap.reporte r1 on r1.repo_id = or1.repo_id 
                   where c1.cuad_id = {cuad_id} and ortr_fecha = {fecha_corte} and ot1.otes_id < 8
                   union all 
                   select ot1.ortr_id, o1.obra_id as repo_id, 99 as reti_id, o1.obra_consecutivo as repo_consecutivo from siap.ordentrabajo ot1
                    inner join siap.cuadrilla c1 on c1.cuad_id = ot1.cuad_id 
                    inner join siap.ordentrabajo_obra oo1 on oo1.ortr_id = ot1.ortr_id 
                    inner join siap.obra o1 on o1.obra_id = oo1.obra_id  
                   where c1.cuad_id = {cuad_id} and ortr_fecha = {fecha_corte} and ot1.otes_id < 8"""
      ).on(
          'cuad_id -> cuad_id,
          'fecha_corte -> fecha_corte
        )
        .as(_parser.*)
        .toIterable
    }
  }

  def obtenerReporteObraPorCuadrillaMobile(
      cuad_id: Long
  ): Future[Iterable[Reporte]] = Future[Iterable[Reporte]] {
    println("Buscando Reportes CuadId: " + cuad_id)
    val fecha_corte = new DateTime() //.toString("yyyy-MM-dd")
    var _listResult: ListBuffer[Reporte] = new ListBuffer()
    val _lista = db.withConnection { implicit connection =>
      val _parser = int("ortr_id") ~ int("repo_id") ~ int("reti_id") ~ int(
        "repo_consecutivo"
      ) map {
        case a1 ~ a2 ~ a3 ~ a4 => (a1, a2, a3, a4)
      }
      println("Cuadrilla Mobile Ejecutando Query")
      val _res = SQL(
        """select ot1.ortr_id, r1.repo_id, r1.reti_id, r1.repo_consecutivo from siap.ordentrabajo ot1
                    inner join siap.cuadrilla c1 on c1.cuad_id = ot1.cuad_id 
                    inner join siap.ordentrabajo_reporte or1 on or1.ortr_id = ot1.ortr_id 
                    inner join siap.reporte r1 on r1.repo_id = or1.repo_id 
                   where c1.cuad_id = {cuad_id} and ortr_fecha = {fecha_corte} and ot1.otes_id < 8 AND r1.rees_id in (1,2)
                   union all 
                   select ot1.ortr_id, o1.obra_id as repo_id, 99 as reti_id, o1.obra_consecutivo as repo_consecutivo from siap.ordentrabajo ot1
                    inner join siap.cuadrilla c1 on c1.cuad_id = ot1.cuad_id 
                    inner join siap.ordentrabajo_obra oo1 on oo1.ortr_id = ot1.ortr_id 
                    inner join siap.obra o1 on o1.obra_id = oo1.obra_id  
                   where c1.cuad_id = {cuad_id} and ortr_fecha = {fecha_corte} and ot1.otes_id < 8 AND o1.rees_id in (1,2)"""
      ).on(
          'cuad_id -> cuad_id,
          'fecha_corte -> fecha_corte
        )
        .as(_parser *)
      println("Cuadrilla Mobile, Resultado: " + _res)
      _res
    }
    println("Lista Orden:" + _lista)
    for (_r <- _lista) {
      println("Reporte Encontrado:" + _r._2 + ", tipo: " + _r._3)
      _r._3 match {
        case 99 => {
          val _obra = obraService.buscarPorId(_r._2)
          _obra match {
            case Some(o) => {
              _listResult += Reporte(
                o.obra_id,
                Some(99),
                Some(99),
                o.obra_consecutivo,
                o.obra_fecharecepcion,
                o.obra_direccion,
                o.obra_descripcion,
                None,
                None,
                None,
                None,
                None,
                o.obra_descripcion,
                None,
                o.rees_id,
                o.orig_id,
                o.barr_id,
                o.empr_id,
                None,
                o.usua_id,
                None,
                None,
                None,
                None,
                None
              )
            }
            case None => {}
          }
        }
        case _ => {
          val _reporte = reporteService.buscarPorId(_r._2)
          _reporte match {
            case Some(r) => {
              _listResult += r
            }
            case None => {}
          }
        }
      }
    }
    _listResult.toList
  }

  def siap_informe_cambios_en_reporte(fecha_inicial: scala.Long, fecha_final: scala.Long, empr_id: scala.Long):Array[Byte] = {
    val empresa = empresaService.buscarPorId(empr_id).get
    //val usuario = usuarioService.buscarPorId(usua_id).get
    var fi = Calendar.getInstance()
    var ff = Calendar.getInstance()
    fi.setTimeInMillis(fecha_inicial)
    ff.setTimeInMillis(fecha_final)
    fi.set(Calendar.MILLISECOND, 0)
    fi.set(Calendar.SECOND, 0)
    fi.set(Calendar.MINUTE, 0)
    fi.set(Calendar.HOUR, 0)

    ff.set(Calendar.MILLISECOND, 59)
    ff.set(Calendar.SECOND, 59)
    ff.set(Calendar.MINUTE, 59)
    ff.set(Calendar.HOUR, 23)
    db.withConnection { implicit connection =>
      val dti = new DateTime(fecha_inicial)
      val dtf = new DateTime(fecha_final)
      val fmdt = DateTimeFormat.forPattern("yyyy-MM-dd HH:mm:ss")
      val sdf = new SimpleDateFormat("yyyy-MM-dd")
      var _listMerged = new ListBuffer[CellRange]()
      var _listRow = new ListBuffer[com.norbitltd.spoiwo.model.Row]()
      var _listColumn = new ListBuffer[com.norbitltd.spoiwo.model.Column]()
      val parser = get[Option[Int]]("ortr_consecutivo") ~
                   get[Option[String]]("cuad_descripcion") ~
                   get[Option[String]]("reti_descripcion") ~
                   get[Option[Int]]("repo_consecutivo") ~
                   get[Option[DateTime]]("repo_fecharecepcion") ~
                   get[Option[DateTime]]("repo_fechasolucion") ~                   
                   get[Option[Int]]("aap_id") ~
                   get[Option[String]]("even_direccion_anterior") ~
                   get[Option[String]]("even_direccion") ~
                   get[Option[String]]("barr_id_anterior") ~
                   get[Option[String]]("barr_id") ~
                   get[Option[String]]("aap_apoyo_anterior") ~
                   get[Option[String]]("aap_apoyo") ~
                   get[Option[String]]("aaco_id_anterior") ~
                   get[Option[String]]("aaco_id") ~
                   get[Option[String]]("aap_tecnologia_anterior") ~
                   get[Option[String]]("aap_tecnologia") ~
                   get[Option[String]]("aap_potencia_anterior") ~
                   get[Option[String]]("aap_potencia") ~
                   get[Option[String]]("aap_potencia_anterior") ~
                   get[Option[String]]("aatc_id_anterior") ~
                   get[Option[String]]("aatc_id") ~
                   get[Option[String]]("aama_id_anterior") ~
                   get[Option[String]]("aama_id") ~
                   get[Option[String]]("aamo_id_anterior") ~
                   get[Option[String]]("aamo_id") ~
                   get[Option[String]]("aacu_id_anterior") ~
                   get[Option[String]]("aacu_id") ~
                   get[Option[String]]("aaus_id_anterior") ~
                   get[Option[String]]("aaus_id") ~
                   get[Option[String]]("medi_id_anterior") ~
                   get[Option[String]]("medi_id") ~
                   get[Option[String]]("tran_id_anterior") ~
                   get[Option[String]]("tran_id") ~
                   get[Option[String]]("aap_lat_anterior") ~
                   get[Option[String]]("aap_lat") ~
                   get[Option[String]]("aap_lng_anterior") ~
                   get[Option[String]]("aap_lng") ~
                   get[Option[String]]("aap_brazo_anterior") ~
                   get[Option[String]]("aap_brazo") ~
                   get[Option[String]]("aap_collarin_anterior") ~
                   get[Option[String]]("aap_collarin") ~
                   get[Option[String]]("aap_poste_propietario_anterior") ~
                   get[Option[String]]("aap_poste_propietario") ~
                   get[Option[String]]("aap_poste_altura_anterior") ~
                   get[Option[String]]("aap_poste_altura") ~
                   get[Option[String]]("tipo_id_anterior") ~
                   get[Option[String]]("tipo_id") map {
                    case a01 ~ a02 ~ a03 ~ a04 ~ a05 ~ a06 ~ a07 ~ a08 ~ a09 ~ a10 ~ 
                         a11 ~ a12 ~ a13 ~ a14 ~ a15 ~ a16 ~ a17 ~ a18 ~ a19 ~ a20 ~ 
                         a21 ~ a22 ~ a23 ~ a24 ~ a25 ~ a26 ~ a27 ~ a28 ~ a29 ~ a30 ~ 
                         a31 ~ a32 ~ a33 ~ a34 ~ a35 ~ a36 ~ a37 ~ a38 ~ a39 ~ a40 ~ 
                         a41 ~ a42 ~ a43 ~ a44 ~ a45 ~ a46 => (
                           (a01, a02, a03, a04, a05, a06, a07, a08, a09, a10), 
                           (a11, a12, a13, a14, a15, a16, a17, a18, a19, a20),
                           (a21, a22, a23, a24, a25, a26, a27, a28, a29, a30), 
                           (a31, a32, a33, a34, a35, a36, a37, a38, a39, a40),
                           (a41, a42, a43, a44, a45, a46))
                   }
      val resultSet = SQL("""
  SELECT 
  ot1.ortr_consecutivo,
  c1.cuad_descripcion,
  rt1.reti_descripcion,
  r1.repo_consecutivo,
  r1.repo_fecharecepcion,
  r1.repo_fechasolucion,  
  rd1.aap_id,
  case 
  	when rd1.even_direccion <> rd1.even_direccion_anterior then rd1.even_direccion_anterior
  	else ''
  end as even_direccion_anterior,
  case 
  	when rd1.even_direccion <> rd1.even_direccion_anterior then rd1.even_direccion
  	else ''
  end as even_direccion,
  case 
  	when rd1.barr_id_anterior <> rd1.barr_id then brd1.barr_descripcion
  	else ''
  end as barr_id_anterior,
  case 
  	when rd1.barr_id_anterior <> rd1.barr_id then brd2.barr_descripcion
  	else ''
  end as barr_id,
  case 
  	when rdda1.aap_apoyo_anterior <> rdda1.aap_apoyo then rdda1.aap_apoyo_anterior
  	else ''
  end as aap_apoyo_anterior,
  case 
  	when rdda1.aap_apoyo_anterior <> rdda1.aap_apoyo then rdda1.aap_apoyo
  	else ''
  end as aap_apoyo,
  case 
  	when rdd1.aaco_id_anterior <> rdd1.aaco_id then ac1.aaco_descripcion
  	else ''
  end as aaco_id_anterior,
  case 
  	when rdd1.aaco_id_anterior <> rdd1.aaco_id then ac2.aaco_descripcion
  	else ''
  end as aaco_id,  
  case 
  	when rdd1.aap_tecnologia_anterior <> rdd1.aap_tecnologia then rdd1.aap_tecnologia_anterior
  	else ''
  end as aap_tecnologia_anterior,
  case 
  	when rdd1.aap_tecnologia_anterior <> rdd1.aap_tecnologia then rdd1.aap_tecnologia
  	else ''
  end as aap_tecnologia,
  case
  	when rdd1.aap_potencia_anterior <> rdd1.aap_potencia then cast(rdd1.aap_potencia_anterior as varchar)
  	else ''
  end as aap_potencia_anterior,
  case
  	when rdd1.aap_potencia_anterior <> rdd1.aap_potencia then cast(rdd1.aap_potencia as varchar)
  	else ''
  end as aap_potencia,  
  case 
  	when rdd1.aatc_id_anterior <> rdd1.aatc_id then atcrdd1.aatc_descripcion
  	else ''
  end as aatc_id_anterior,
  case 
  	when rdd1.aatc_id_anterior <> rdd1.aatc_id then atcrdd2.aatc_descripcion
  	else ''
  end as aatc_id,
  case
  	when rdd1.aama_id_anterior <> rdd1.aama_id then amardd1.aama_descripcion
  	else ''
  end as aama_id_anterior,
  case
  	when rdd1.aama_id_anterior <> rdd1.aama_id then amardd2.aama_descripcion
  	else ''
  end as aama_id,  
  case 
  	when rdd1.aamo_id_anterior <> rdd1.aamo_id then amordd1.aamo_descripcion
  	else ''
  end as aamo_id_anterior,
  case 
  	when rdd1.aamo_id_anterior <> rdd1.aamo_id then amordd2.aamo_descripcion
  	else ''
  end as aamo_id,
  case 
  	when rdda1.aacu_id_anterior <> rdda1.aacu_id then acu1.aacu_descripcion
  	else ''
  end as aacu_id_anterior,
  case 
  	when rdda1.aacu_id_anterior <> rdda1.aacu_id then acu2.aacu_descripcion
  	else ''
  end as aacu_id,
  case
  	when rdda1.aaus_id_anterior <> rdda1.aaus_id then aus1.aaus_descripcion
  	else ''
  end as aaus_id_anterior,
  case
  	when rdda1.aaus_id_anterior <> rdda1.aaus_id then aus2.aaus_descripcion
  	else ''
  end as aaus_id,
  case 
  	when rdda1.medi_id_anterior <> rdda1.medi_id then m1.medi_numero
  	else ''
  end as medi_id_anterior,
  case 
  	when rdda1.medi_id_anterior <> rdda1.medi_id then m2.medi_numero
  	else ''
  end as medi_id,
  case 
  	when rdda1.tran_id_anterior <> rdda1.tran_id then t1.aap_numero
  	else ''
  end as tran_id_anterior,
  case 
  	when rdda1.tran_id_anterior <> rdda1.tran_id then t2.aap_numero
  	else ''
  end as tran_id,
  case 
  	when rdda1.aap_lat_anterior <> rdda1.aap_lat then rdda1.aap_lat_anterior
  	else ''
  end as aap_lat_anterior,
  case 
  	when rdda1.aap_lat_anterior <> rdda1.aap_lat then rdda1.aap_lat
  	else ''
  end as aap_lat,
  case
  	when rdda1.aap_lng_anterior <> rdda1.aap_lng then rdda1.aap_lng_anterior
  	else ''
  end as aap_lng_anterior,
  case
  	when rdda1.aap_lng_anterior <> rdda1.aap_lng then rdda1.aap_lng
  	else ''
  end as aap_lng,
  case 
  	when rdd1.aap_brazo_anterior <> rdd1.aap_brazo then rdd1.aap_brazo_anterior
  	else ''
  end as aap_brazo_anterior,
  case 
  	when rdd1.aap_brazo_anterior <> rdd1.aap_brazo then rdd1.aap_brazo
  	else ''
  end as aap_brazo,
  case 
  	when rdd1.aap_collarin_anterior <> rdd1.aap_collarin then rdd1.aap_collarin_anterior
  	else ''
  end as aap_collarin_anterior,
  case 
  	when rdd1.aap_collarin_anterior <> rdd1.aap_collarin then rdd1.aap_collarin
  	else ''
  end as aap_collarin,
  case 
  	when rdd1.aap_poste_propietario_anterior <> rdd1.aap_poste_propietario then rdd1.aap_poste_propietario_anterior
  	else ''
  end as aap_poste_propietario_anterior,
  case 
  	when rdd1.aap_poste_propietario_anterior <> rdd1.aap_poste_propietario then rdd1.aap_poste_propietario
  	else ''
  end as aap_poste_propietario,
  case 
  	when rdd1.aap_poste_altura_anterior <> rdd1.aap_poste_altura then cast(rdd1.aap_poste_altura_anterior as varchar)
  	else ''
  end as aap_poste_altura_anterior,
  case 
  	when rdd1.aap_poste_altura_anterior <> rdd1.aap_poste_altura then cast(rdd1.aap_poste_altura as varchar)
  	else ''
  end as aap_poste_altura,
  case 
  	when rdd1.tipo_id_anterior <> rdd1.tipo_id then tp1.tipo_descripcion
  	else ''
  end as tipo_id_anterior,
  case 
  	when rdd1.tipo_id_anterior <> rdd1.tipo_id then tp2.tipo_descripcion
  	else ''
  end as tipo_id
from siap.reporte r1
inner join siap.reporte_direccion rd1 on rd1.repo_id = r1.repo_id 
inner join siap.reporte_direccion_dato rdd1 on rdd1.repo_id = rd1.repo_id and rdd1.aap_id = rd1.aap_id and rdd1.even_id = rd1.even_id and rd1.even_estado <> 9
inner join siap.reporte_direccion_dato_adicional rdda1 on rdda1.repo_id = rd1.repo_id and rdda1.aap_id = rd1.aap_id and rdda1.even_id = rd1.even_id
inner join siap.ordentrabajo_reporte otr1 on otr1.tireuc_id = r1.tireuc_id and otr1.repo_id = r1.repo_id
inner join siap.ordentrabajo ot1 on ot1.ortr_id = otr1.ortr_id and ot1.ortr_fecha = r1.repo_fechasolucion 
inner join siap.cuadrilla c1 on c1.cuad_id = ot1.cuad_id
left join siap.reporte_tipo rt1 on rt1.reti_id = r1.reti_id
left join siap.barrio brd1 on brd1.barr_id = rd1.barr_id_anterior 
left join siap.barrio brd2 on brd2.barr_id = rd1.barr_id
left join siap.aap_conexion ac1 on ac1.aaco_id = rdd1.aaco_id_anterior
left join siap.aap_conexion ac2 on ac2.aaco_id = rdd1.aaco_id
left join siap.aap_tipo_carcasa atcrdd1 on atcrdd1.aatc_id = rdd1.aatc_id_anterior 
left join siap.aap_tipo_carcasa atcrdd2 on atcrdd2.aatc_id = rdd1.aatc_id
left join siap.aap_marca amardd1 on amardd1.aama_id = rdd1.aama_id_anterior  
left join siap.aap_marca amardd2 on amardd2.aama_id = rdd1.aama_id
left join siap.aap_modelo amordd1 on amordd1.aamo_id = rdd1.aamo_id_anterior  
left join siap.aap_modelo amordd2 on amordd2.aamo_id = rdd1.aamo_id
left join siap.aap_cuentaap acu1 on acu1.aacu_id = rdda1.aacu_id_anterior 
left join siap.aap_cuentaap acu2 on acu2.aacu_id = rdda1.aacu_id
left join siap.aap_uso aus1 on aus1.aaus_id = rdda1.aaus_id_anterior 
left join siap.aap_uso aus2 on aus2.aaus_id = rdda1.aaus_id
left join siap.medidor m1 on m1.medi_id = rdda1.medi_id_anterior 
left join siap.medidor m2 on m2.medi_id = rdda1.medi_id 
left join siap.transformador t1 on t1.aap_id = rdda1.tran_id_anterior 
left join siap.transformador t2 on t2.aap_id = rdda1.tran_id
left join siap.tipo_poste tp1 on tp1.tipo_id = rdd1.tipo_id_anterior
left join siap.tipo_poste tp2 on tp2.tipo_id = rdd1.tipo_id
where r1.empr_id = {empr_id} and
r1.repo_fechasolucion between {fecha_inicial} and {fecha_final} and r1.reti_id <> 2 and r1.rees_id < 9 and
(rd1.even_direccion <> rd1.even_direccion_anterior or 
rd1.barr_id <> rd1.barr_id_anterior or
rdda1.aap_apoyo <> rdda1.aap_apoyo_anterior or
rdd1.aaco_id <> rdd1.aaco_id_anterior or
rdd1.aatc_id <> rdd1.aatc_id_anterior or
rdd1.aama_id <> rdd1.aama_id_anterior or
rdd1.aamo_id <> rdd1.aamo_id_anterior or
rdd1.aap_potencia <> rdd1.aap_potencia_anterior or
rdd1.aap_tecnologia <> rdd1.aap_tecnologia_anterior or
rdd1.aap_brazo <> rdd1.aap_brazo_anterior or
rdd1.aap_collarin <> rdd1.aap_collarin_anterior or
rdd1.aap_poste_altura <> rdd1.aap_poste_altura_anterior or
rdd1.aap_poste_propietario <> rdd1.aap_poste_propietario_anterior or
rdda1.aacu_id <> rdda1.aacu_id_anterior or
rdda1.aaus_id <> rdda1.aaus_id_anterior or
rdda1.medi_id <> rdda1.medi_id_anterior or
rdda1.tran_id <> rdda1.tran_id_anterior or
rdda1.aap_lat <> rdda1.aap_lat_anterior or
rdda1.aap_lng <> rdda1.aap_lng_anterior or
rdd1.tipo_id <> rdd1.tipo_id_anterior
)
order by ot1.ortr_consecutivo, c1.cuad_descripcion 
    
       """).
      on(
        'empr_id -> empr_id,
        'fecha_inicial -> dti,
        'fecha_final -> dtf
      ).as(InformeCambios._set *)

      val sheet01 = Sheet(
          name = "Cambios de Información",
          rows = {
            _listRow += com.norbitltd.spoiwo.model
              .Row()
              .withCellValues(empresa.empr_descripcion)
            _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("Informe de Cambios de Información en Reportes")
            _listRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "Desde:",
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              DateCell(
                dti.toDate(),
                Some(1),
                style = Some(
                  CellStyle(dataFormat = CellDataFormat("YYYY/MM/DD"))
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Hasta:",
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              DateCell(
                dtf.toDate(),
                Some(3),
                style = Some(
                  CellStyle(dataFormat = CellDataFormat("YYYY/MM/DD"))
                ),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              )
            )
            _listRow += com.norbitltd.spoiwo.model.Row().withCellValues("Generado el: ", fmdt.print(DateTime.now()))
            _listRow += com.norbitltd.spoiwo.model.Row(
              StringCell(
                "OT No.",
                Some(0),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Cuadrilla",
                Some(1),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Tipo de Reporte",
                Some(2),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Consecutivo Reporte",
                Some(3),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Fecha Recepción",
                Some(4),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Fecha Solución",
                Some(5),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Luminaria",
                Some(6),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Dirección Ant.",
                Some(7),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Dirección Nue.",
                Some(8),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Barrio Ant.",
                Some(9),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Barrio Nue.",
                Some(10),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Apoyo Ant.",
                Some(11),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Apoyo Nue.",
                Some(12),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Conexión Ant.",
                Some(13),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Conexión Nue.",
                Some(14),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Tecnología Ant.",
                Some(15),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Tecnología Nue.",
                Some(16),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Potencia Ant.",
                Some(17),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Potencia Nue.",
                Some(18),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Tipo de Luminaria Ant.",
                Some(19),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Tipo de Luminaria Nue.",
                Some(20),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Marca Ant.",
                Some(21),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Marca Nue.",
                Some(22),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Modelo Ant.",
                Some(23),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Modelo Nue.",
                Some(24),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Cuenta Ant.",
                Some(25),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Cuenta Nue.",
                Some(26),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Uso Ant.",
                Some(27),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Uso Nue.",
                Some(28),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Medidor Ant.",
                Some(29),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Medidor Nue.",
                Some(30),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Transformador Ant.",
                Some(31),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Transformador Nue.",
                Some(32),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Latitud Ant.",
                Some(33),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Latitud Nue.",
                Some(34),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Longitud Ant.",
                Some(35),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Longitud Nue.",
                Some(36),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Brazo Ant.",
                Some(37),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Brazo Nue.",
                Some(38),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Collarin Ant.",
                Some(39),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Collarin Nue.",
                Some(40),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Poste Propietario Ant.",
                Some(41),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Poste Propietario Nue.",
                Some(42),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Poste Altura Ant.",
                Some(43),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Poste Altura Nue.",
                Some(44),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Poste Tipo Ant.",
                Some(45),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
              StringCell(
                "Poste Tipo Nue.",
                Some(46),
                style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                CellStyleInheritance.CellThenRowThenColumnThenSheet
              ),
            )
            resultSet.map { row =>
              _listRow += com.norbitltd.spoiwo.model.Row(
                StringCell(
                  row.ortr_consecutivo match { case Some(x) => x.toString case None => "" },
                  Some(0),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.cuad_descripcion match { case Some(x) => x.toString case None => "" },
                  Some(1),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.reti_descripcion match { case Some(x) => x.toString case None => "" },
                  Some(2),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.repo_consecutivo match { case Some(x) => x.toString case None => "" },
                  Some(3),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.repo_fecharecepcion match { case Some(x) => x.toString("yyyy-MM-dd HH:mm:ss") case None => "" },
                  Some(4),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.repo_fechasolucion match { case Some(x) => x.toString("yyyy-MM-dd") case None => "" },
                  Some(5),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_id match { case Some(x) => x.toString() case None => "" },
                  Some(6),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.even_direccion_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(7),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.even_direccion match { case Some(x) => x.toString() case None => "" },
                  Some(8),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.barr_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(9),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.barr_id match { case Some(x) => x.toString() case None => "" },
                  Some(10),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_apoyo_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(11),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_apoyo match { case Some(x) => x.toString() case None => "" },
                  Some(12),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aaco_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(13),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aaco_id match { case Some(x) => x.toString() case None => "" },
                  Some(14),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_tecnologia_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(15),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_tecnologia match { case Some(x) => x.toString() case None => "" },
                  Some(16),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_potencia_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(17),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_potencia match { case Some(x) => x.toString() case None => "" },
                  Some(18),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aatc_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(19),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aatc_id match { case Some(x) => x.toString() case None => "" },
                  Some(20),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aama_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(21),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aama_id match { case Some(x) => x.toString() case None => "" },
                  Some(22),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aamo_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(23),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aamo_id match { case Some(x) => x.toString() case None => "" },
                  Some(24),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aacu_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(25),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aacu_id match { case Some(x) => x.toString() case None => "" },
                  Some(26),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aaus_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(27),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aaus_id match { case Some(x) => x.toString() case None => "" },
                  Some(28),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.medi_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(29),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.medi_id match { case Some(x) => x.toString() case None => "" },
                  Some(30),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.tran_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(31),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.tran_id match { case Some(x) => x.toString() case None => "" },
                  Some(32),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_lat_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(33),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_lat match { case Some(x) => x.toString() case None => "" },
                  Some(34),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_lng_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(35),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_lng match { case Some(x) => x.toString() case None => "" },
                  Some(36),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_brazo_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(37),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_brazo match { case Some(x) => x.toString() case None => "" },
                  Some(38),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_collarin_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(39),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_collarin match { case Some(x) => x.toString() case None => "" },
                  Some(40),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_poste_propietario_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(41),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_poste_propietario match { case Some(x) => x.toString() case None => "" },
                  Some(42),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_poste_altura_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(43),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.aap_poste_altura match { case Some(x) => x.toString() case None => "" },
                  Some(44),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.tipo_id_anterior match { case Some(x) => x.toString() case None => "" },
                  Some(45),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                ),
                StringCell(
                  row.tipo_id match { case Some(x) => x.toString() case None => "" },
                  Some(46),
                  style = Some(CellStyle(dataFormat = CellDataFormat("@"))),
                  CellStyleInheritance.CellThenRowThenColumnThenSheet
                )
              )
            }
            _listRow.toList
          },
          mergedRegions = {
              _listMerged += CellRange((0, 0), (0, 5))
              _listMerged += CellRange((1, 1), (0, 5))
              _listMerged.toList
            },
          columns = {
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 1, width = new Width(15, WidthUnit.Character))
               _listColumn += com.norbitltd.spoiwo.model
                .Column(index = 2, width = new Width(30, WidthUnit.Character))                
              _listColumn.toList
            }
      )
      println("Escribiendo en el Stream")
      var os: ByteArrayOutputStream = new ByteArrayOutputStream()
      Workbook(sheet01).writeToOutputStream(os)
      println("Stream Listo")
      os.toByteArray
    }
  }
}
