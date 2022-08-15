package models

import anorm._
import anorm.SqlParser.{get, str}
import anorm.JodaParameterMetaData._

import javax.inject.Inject
import java.util.Calendar
import java.util.UUID
import java.text.SimpleDateFormat
import java.io.File
import java.io.FileInputStream

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel._

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._
import play.api.libs.EventSource.Event

import play.api.db.DBApi

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}
import scala.math.BigDecimal
import scala.collection.JavaConverters._

import anorm.SqlParser.{ get, str, int, float }

import org.joda.time.DateTime
import org.joda.time.LocalDateTime
import org.joda.time.LocalDate
import java.io.ByteArrayInputStream

import scala.util.control.NonFatal

import java.io.{BufferedWriter, FileOutputStream, OutputStreamWriter}

import com.univocity.parsers.csv.{CsvWriter, CsvWriterSettings}

import services.ProcessingProgressTrackerEvent.registerNewStatus

import utilities._

case class ControlCarga(
    controlcarga_id: Option[Long],
    controlcarga_anho: Option[Int],
    controlcarga_mes: Option[Int],
    controlcarga_fecha: Option[DateTime],
    usua_id: Option[Long],
    empr_id: Option[Long],
    controlcarga_registros: Option[Long],
    controlcarga_exito: Option[Boolean],
    controlcarga_mensaje: Option[String],
    controlcarga_estado: Option[Int]
)

object ControlCarga {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  val _set = {
    get[Option[Long]]("controlcarga_id") ~
      get[Option[Int]]("controlcarga_anho") ~
      get[Option[Int]]("controlcarga_mes") ~
      get[Option[DateTime]]("controlcarga_fecha") ~
      get[Option[Long]]("usua_id") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("controlcarga_registros") ~
      get[Option[Boolean]]("controlcarga_exito") ~
      get[Option[String]]("controlcarga_mensaje") ~
      get[Option[Int]]("controlcarga_estado") map {
        case controlcarga_id ~
            controlcarga_anho ~
            controlcarga_mes ~
            controlcarga_fecha ~
            usua_id ~
            empr_id ~
            controlcarga_registros ~
            controlcarga_exito ~
            controlcarga_mensaje ~
            controlcarga_estado =>
          ControlCarga(
            controlcarga_id,
            controlcarga_anho,
            controlcarga_mes,
            controlcarga_fecha,
            usua_id,
            empr_id,
            controlcarga_registros,
            controlcarga_exito,
            controlcarga_mensaje,
            controlcarga_estado
          )
      }
  }

}

class CargaRepository @Inject() (
    dbapi: DBApi
)(implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")
  private val df = new DataFormatter(true)
  private val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  private var c_id = 0L
  private var e_id = 0L
  private var anho = 0
  private var mes = 0
  /* Procesar archivos
   */

  df.addFormat("-1.234", new java.text.DecimalFormat("0"))
  df.addFormat("-1,234", new java.text.DecimalFormat("0"))

  def ultimaCarga(empr_id: Long): (String, String, Int, Int) = {
    db.withConnection { implicit connection => 
      val parser = int("controlcarga_anho") ~ int("controlcarga_mes") map { case a ~ b => (a,b) }
      val periodo = SQL("""SELECT cc1.controlcarga_anho, cc1.controlcarga_mes FROM siap.controlcarga cc1
             WHERE cc1.controlcarga_estado = 1 AND cc1.empr_id = {empr_id}
             ORDER BY controlcarga_anho DESC, controlcarga_mes DESC
             LIMIT 1""").
             on(
               'empr_id -> empr_id
             )
             .as(parser.single)
      val aDate: LocalDate = new LocalDate(periodo._1, periodo._2, 1)
      val lastDay = aDate.dayOfMonth().getMaximumValue();
      val conDia = Utility.mes(periodo._2) + " " + lastDay + " de " + periodo._1
      val sinDia = Utility.mes(periodo._2) + " de " + periodo._1
      (sinDia, conDia, periodo._1, periodo._2)
    }
  }

  def removeLineBreakingChars(cell: String): String =
    cell.replaceAll("[\\t\\n\\r]", " ")

  def eliminar_1(
      a: Int,
      m: Int,
      empr_id: Long,
      usua_id: Long
  ) = {
    db.withConnection { implicit connection =>
      val result = SQL("""DELETE FROM siap.medidor_lectura cd 
               WHERE 
               cd.mele_anho = {mele_anho} and
               cd.mele_mes = {mele_mes}""")
        .on(
          'mele_anho -> a,
          'mele_mes -> m
        )
        .executeUpdate() > 0
      if (result) {
        SQL("""UPDATE siap.controlcarga SET controlcarga_estado = 9 WHERE
                    empr_id = {empr_id} and 
                    controlcarga_anho = {anho} and
                    controlcarga_mes = {mes} and 
                    controlcarga_tipo = {tipo}""")
          .on(
            'empr_id -> empr_id,
            'anho -> a,
            'mes -> m,
            'tipo -> 1
          )
          .executeUpdate()
      }
      result
    }
  }

  def cuenta(empr_id: Long, tipo: Int): Long = {
    db.withConnection { implicit connection =>
      val result = SQL(
        """SELECT COUNT(*) AS c FROM siap.controlcarga c 
                           WHERE c.empr_id = {empr_id} and c.controlcarga_tipo = {tipo}"""
      ).on(
        'empr_id -> empr_id,
        'tipo -> tipo
      ).as(SqlParser.scalar[Long].single)
      result
    }
  }

  def todos(
      empr_id: Long,
      tipo: Int,
      page_size: Long,
      current_page: Long
  ): Future[Iterable[ControlCarga]] = Future[Iterable[ControlCarga]] {
    db.withConnection { implicit connection =>
      SQL(
        """SELECT c.* FROM siap.controlcarga c 
                      WHERE c.empr_id = {empr_id} and c.controlcarga_tipo = {tipo} ORDER BY controlcarga_anho DESC, controlcarga_mes DESC LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
      ).on(
        'empr_id -> empr_id,
        'tipo -> tipo,
        'page_size -> page_size,
        'current_page -> current_page
      ).as(ControlCarga._set *)
    }
  }
}
