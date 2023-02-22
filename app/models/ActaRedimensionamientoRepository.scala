package models

import javax.inject.Inject
import java.util.Calendar
import java.io.{OutputStream, ByteArrayOutputStream, FileInputStream}
import java.util.{Map, HashMap, Date}
import java.sql.Date
import java.text.SimpleDateFormat
import java.io.ByteArrayOutputStream
import java.util.UUID.randomUUID

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

case class ActaRedimensionamiento(
    acre_id: Option[Long],
    acre_numero: Option[Int],
    acre_anho: Option[Int],
    acre_periodo: Option[Int],
    acre_fecha: Option[LocalDate],
    acre_valor_acumulado: Option[Double],
    empr_id: Option[Long],
    usua_id: Option[Long]
)

object ActaRedimensionamiento {
    val _set = {
        get[Option[Long]]("acre_id") ~
        get[Option[Int]]("acre_numero") ~
        get[Option[Int]]("acre_anho") ~
        get[Option[Int]]("acre_periodo") ~
        get[Option[LocalDate]]("acre_fecha") ~
        get[Option[Double]]("acre_valor_acumulado") ~
        get[Option[Long]]("empr_id") ~
        get[Option[Long]]("usua_id") map {
            case acre_id ~ acre_numero ~ acre_anho ~ acre_periodo ~ acre_fecha ~ acre_valor_acumulado ~ empr_id ~ usua_id =>
            ActaRedimensionamiento(acre_id, acre_numero, acre_anho, acre_periodo, acre_fecha, acre_valor_acumulado, empr_id, usua_id)
        }
    }
}

class ActaRedimensionamientoRepository @Inject()(
    dbapi: DBApi,
    usuarioService: UsuarioRepository,
    empresaService: EmpresaRepository
)(implicit ec: DatabaseExecutionContext) {

  private val db = dbapi.database("default")

  /**
    * Recuperar total de registros
    * @return total
    */
  def cuenta(empr_id: Long, filter: String): Long = {
    db.withConnection { implicit connection =>
      var query: String =
        """
        SELECT COUNT(*) AS c FROM siap.acta_redimensionamiento ar1
        WHERE a.empr_id = {empr_id}
      """
      if (!filter.isEmpty) {
        println("Filtro: " + filter)
        query = query + " and " + filter
      }
      val result = SQL(query)
        .on(
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[Long].single)
      result
    }
  }  

  def getActasRedimensionamiento(empr_id: Long, page_size: Long, current_page: Long, orderby: String, filter: String): Future[List[ActaRedimensionamiento]] = Future[List[ActaRedimensionamiento]] {
    db.withConnection { implicit connection =>
      var query: String = "SELECT * FROM siap.acta_redimensionamiento WHERE empr_id = {empr_id}"
      var curr_page: Long = current_page
      if (!filter.isEmpty) {
        query = query + " and " + filter
        curr_page = 1
      }
      if (!orderby.isEmpty) {
        query = query + s" ORDER BY $orderby"
      } else {
        query = query + s" ORDER BY acre_id asc"
      }
      query = query + """
                        LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)"""
      val _lista = SQL(query)
        .on(
          'empr_id -> empr_id,
          'page_size -> page_size,
          'current_page -> curr_page
        )
        .as(ActaRedimensionamiento._set *)
        _lista.toList
    }
  }
}