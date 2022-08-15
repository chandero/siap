package models

import javax.inject.Inject
import java.util.Calendar
import java.io.File
import java.text.SimpleDateFormat
import java.util.UUID
import java.io.{BufferedWriter, FileInputStream, FileOutputStream, OutputStreamWriter}

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{get, str}
import anorm.JodaParameterMetaData._

import scala.util.{Failure, Success}
import scala.concurrent.{Await, Future}

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

import org.apache.poi.xssf.streaming.SXSSFWorkbook
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import org.apache.poi.hssf.usermodel.HSSFWorkbook
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.DataFormatter
import org.apache.poi.ss.usermodel.WorkbookFactory
import org.apache.poi.ss.usermodel.FormulaEvaluator

import scala.util.control.NonFatal


import com.univocity.parsers.csv.{CsvWriter, CsvWriterSettings}

import services.ProcessingProgressTrackerEvent.registerNewStatus

case class Medidor_Tabla_Dato(
    metd_id: Option[Long],
    metd_descripcion: Option[String]
)

case class Medidor_Dato(
    meda_id: Option[Long],
    metd_id: Option[Long],
    meda_activa: Option[String],
    meda_reactiva: Option[String],
    meda_nuevo: Option[String]
)

case class Medidor(
    medi_id: Option[Long],
    medi_numero: Option[String],
    amem_id: Option[Long],
    amet_id: Option[Long],
    aacu_id: Option[Long],
    empr_id: Option[Long],
    usua_id: Option[Long],
    medi_direccion: Option[String],
    medi_estado: Option[Int],
    medi_acta: Option[String],
    datos: Option[List[Medidor_Dato]]
)

case class MedidorLectura(
    medi_id: Option[Long],
    medi_numero: Option[String],
    mele_anho: Option[Int],
    mele_mes: Option[Int],
    mele_activa_anterior: Option[Int],
    mele_activa_actual: Option[Int],
    mele_reactiva: Option[Int],
    mele_reactiva_anterior: Option[Int],
    mele_reactiva_actual: Option[Int]
)

case class Informe(
    medi_codigo: Option[String],
    medi_numero: Option[String],
    medi_direccion: Option[String],
    aacu_descripcion: Option[String],
    cantidad: Option[Int]
)

object Medidor_Tabla_Dato {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val mdWrites = new Writes[Medidor_Tabla_Dato] {
    def writes(m: Medidor_Tabla_Dato) = Json.obj(
      "metd_id" -> m.metd_id,
      "metd_descripcion" -> m.metd_descripcion
    )
  }
  implicit val mReads: Reads[Medidor_Tabla_Dato] = (
    (__ \ "metd_id").readNullable[Long] and
      (__ \ "metd_descripcion").readNullable[String]
  )(Medidor_Tabla_Dato.apply _)

  val _set = {
    get[Option[Long]]("metd_id") ~
      get[Option[String]]("metd_descripcion") map {
      case metd_id ~
            metd_descripcion =>
        Medidor_Tabla_Dato(
          metd_id,
          metd_descripcion
        )
    }
  }
}

object Medidor_Dato {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val mdWrites = new Writes[Medidor_Dato] {
    def writes(m: Medidor_Dato) = Json.obj(
      "meda_id" -> m.meda_id,
      "metd_id" -> m.metd_id,
      "meda_activa" -> m.meda_activa,
      "meda_reactiva" -> m.meda_activa,
      "meda_nuevo" -> m.meda_nuevo
    )
  }
  implicit val mReads: Reads[Medidor_Dato] = (
    (__ \ "meda_id").readNullable[Long] and
      (__ \ "metd_id").readNullable[Long] and
      (__ \ "meda_activa").readNullable[String] and
      (__ \ "meda_reactiva").readNullable[String] and
      (__ \ "meda_nuevo").readNullable[String]
  )(Medidor_Dato.apply _)

  val _set = {
    get[Option[Long]]("meda_id") ~
      get[Option[Long]]("metd_id") ~
      get[Option[String]]("meda_activa") ~
      get[Option[String]]("meda_reactiva") ~
      get[Option[String]]("meda_nuevo") map {
      case meda_id ~
            metd_id ~
            meda_activa ~
            meda_reactiva ~
            meda_nuevo =>
        Medidor_Dato(
          meda_id,
          metd_id,
          meda_activa,
          meda_reactiva,
          meda_nuevo
        )
    }
  }
}

object Medidor {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val medidorWrites = new Writes[Medidor] {
    def writes(m: Medidor) = Json.obj(
      "medi_id" -> m.medi_id,
      "medi_numero" -> m.medi_numero,
      "amem_id" -> m.amem_id,
      "amet_id" -> m.amet_id,
      "aacu_id" -> m.aacu_id,
      "empr_id" -> m.empr_id,
      "usua_id" -> m.usua_id,
      "medi_direccion" -> m.medi_direccion,
      "medi_estado" -> m.medi_estado,
      "medi_acta" -> m.medi_acta,
      "datos" -> m.datos
    )
  }

  implicit val medidorReads: Reads[Medidor] = (
    (__ \ "medi_id").readNullable[Long] and
      (__ \ "medi_numero").readNullable[String] and
      (__ \ "amem_id").readNullable[Long] and
      (__ \ "amet_id").readNullable[Long] and
      (__ \ "aacu_id").readNullable[Long] and
      (__ \ "empr_id").readNullable[Long] and
      (__ \ "usua_id").readNullable[Long] and
      (__ \ "medi_direccion").readNullable[String] and
      (__ \ "medi_estado").readNullable[Int] and
      (__ \ "medi_acta").readNullable[String] and
      (__ \ "datos").readNullable[List[Medidor_Dato]]
  )(Medidor.apply _)

  val _set = {
    get[Option[Long]]("medi_id") ~
      get[Option[String]]("medi_numero") ~
      get[Option[Long]]("amem_id") ~
      get[Option[Long]]("amet_id") ~
      get[Option[Long]]("aacu_id") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("usua_id") ~
      get[Option[String]]("medi_direccion") ~
      get[Option[Int]]("medi_estado") ~
      get[Option[String]]("medi_acta") map {
      case medi_id ~
            medi_numero ~
            amem_id ~
            amet_id ~
            aacu_id ~
            empr_id ~
            usua_id ~
            medi_direccion ~
            medi_estado ~
            medi_acta =>
        Medidor(
          medi_id,
          medi_numero,
          amem_id,
          amet_id,
          aacu_id,
          empr_id,
          usua_id,
          medi_direccion,
          medi_estado,
          medi_acta,
          None
        )
    }
  }
}

object MedidorLectura {
  var _set = {
    get[Option[Long]]("medi_id") ~
      get[Option[String]]("medi_numero") ~
      get[Option[Int]]("mele_anho") ~
      get[Option[Int]]("mele_mes") ~
      get[Option[Int]]("mele_activa_anterior") ~
      get[Option[Int]]("mele_activa_actual") ~
      get[Option[Int]]("mele_reactiva") ~
      get[Option[Int]]("mele_reactiva_anterior") ~
      get[Option[Int]]("mele_reactiva_actual") map {
      case medi_id ~
            medi_numero ~
            mele_anho ~
            mele_mes ~
            mele_activa_anterior ~
            mele_activa_actual ~
            mele_reactiva ~
            mele_reactiva_anterior ~
            mele_reactiva_actual =>
        new MedidorLectura(
          medi_id,
          medi_numero,
          mele_anho,
          mele_mes,
          mele_activa_anterior,
          mele_activa_actual,
          mele_reactiva,
          mele_reactiva_anterior,
          mele_reactiva_actual
        )
    }
  }
}

object Informe {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val mWrites = new Writes[Informe] {
    def writes(m: Informe) = Json.obj(
      "medi_codigo" -> m.medi_codigo,
      "medi_numero" -> m.medi_numero,
      "medi_direccion" -> m.medi_direccion,
      "aacu_descripcion" -> m.aacu_descripcion,
      "cantidad" -> m.cantidad
    )
  }

  val _set = {
    get[Option[String]]("medi_codigo") ~
      get[Option[String]]("medi_numero") ~
      get[Option[String]]("medi_direccion") ~
      get[Option[String]]("aacu_descripcion") ~
      get[Option[Int]]("cantidad") map {
      case medi_codigo ~
            medi_numero ~
            medi_direccion ~
            aacu_descripcion ~
            cantidad =>
        Informe(
          medi_codigo,
          medi_numero,
          medi_direccion,
          aacu_descripcion,
          cantidad
        )
    }
  }
}

class MedidorRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext
) {
  private val db = dbapi.database("default")
  private val df = new DataFormatter(true)
  private val sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
  private var c_id = 0L
  private var e_id = 0L
  private var anho = 0
  private var mes = 0
  /**
    * Obtener Lista de Medidor_Tabla_Dato desde un ResultSet
    */
  def medidor_tabla_dato(): Future[Iterable[Medidor_Tabla_Dato]] =
    Future[Iterable[Medidor_Tabla_Dato]] {
      db.withConnection { implicit connection =>
        SQL("""SELECT * FROM siap.medidor_tabla_dato""").as(
          Medidor_Tabla_Dato._set *
        )
      }
    }

  /**
    * Recuperar un Medidor dado su medi_id
    * @param medi_id: Long
    */
  def buscarPorId(medi_id: Long): Option[Medidor] = {
    db.withConnection { implicit connection =>
      val m = SQL("SELECT * FROM siap.medidor WHERE medi_id = {medi_id}")
        .on(
          'medi_id -> medi_id
        )
        .as(Medidor._set.singleOpt)

      val datos = SQL(
        """SELECT * FROM siap.medidor_dato md WHERE md.medi_id = {medi_id}"""
      ).on(
          'medi_id -> medi_id
        )
        .as(Medidor_Dato._set *)

      val medidor = m.get.copy(datos = Some(datos))
      Some(medidor)
    }
  }

  /**
    * Recuperar un Medidor dado su medi_numero
    * @param medi_id: Long
    */
  def buscarPorNumero(medi_numero: String): Option[Medidor] = {
    db.withConnection { implicit connection =>
      val m = SQL("SELECT * FROM siap.medidor WHERE medi_numero = {medi_numero}")
        .on(
          'medi_numero -> medi_numero
        )
        .as(Medidor._set.singleOpt)

      m match {
        case Some(m) => val datos = SQL(
            """SELECT * FROM siap.medidor_dato md WHERE md.medi_id = {medi_id}"""
            ).on(
              'medi_id -> m.medi_id
            )
            .as(Medidor_Dato._set *)

            val medidor = m.copy(datos = Some(datos))
            Some(medidor)
        case None => m
      }
    }
  }    

  /**
    * Recuperar un Medidor dado su medi_comenergia
    * @param medi_id: Long
    */
  def buscarPorComenergia(medi_comenergia: String): Option[Medidor] = {
    db.withConnection { implicit connection =>
      val m = SQL("SELECT * FROM siap.medidor WHERE medi_comenergia = {medi_comenergia}")
        .on(
          'medi_comenergia -> medi_comenergia
        )
        .as(Medidor._set.singleOpt)

      m match {
        case Some(m) => val datos = SQL(
            """SELECT * FROM siap.medidor_dato md WHERE md.medi_id = {medi_id}"""
            ).on(
              'medi_id -> m.medi_id
            )
            .as(Medidor_Dato._set *)

            val medidor = m.copy(datos = Some(datos))
            Some(medidor)
        case None => m
      }
    }
  }  

  /**
    * Recuperar total de registros
    * @return total
    */
  def cuenta(empr_id: Long): Long = {
    db.withConnection { implicit connection =>
      val result = SQL(
        "SELECT COUNT(*) AS c FROM siap.medidor WHERE medi_estado <> 9 and empr_id = {empr_id}"
      ).on(
          'empr_id -> empr_id
        )
        .as(SqlParser.scalar[Long].single)
      result
    }
  }

  /**
    * Recuperar todos los Medidor activos
    * @param page_size: Long
    * @param current_page: Long
    * @param empr_id: Long
    */
  def todos(
      page_size: Long,
      current_page: Long,
      empr_id: Long
  ): Future[Iterable[Medidor]] = Future[Iterable[Medidor]] {
    db.withConnection { implicit connection =>
      SQL(
        "SELECT * FROM siap.medidor WHERE medi_estado <> 9 and empr_id = {empr_id} LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1) ORDER BY medi_id"
      ).on(
          'page_size -> page_size,
          'current_page -> current_page,
          'empr_id -> empr_id
        )
        .as(Medidor._set *)
    }
  }

  /**
    * Recuperar todos los TipoMedidor activos
    * @param page_size: Long
    * @param current_page: Long
    */
  def medidores(empr_id: Long): Future[Iterable[Medidor]] =
    Future[Iterable[Medidor]] {
      db.withConnection { implicit connection =>
        SQL(
          "SELECT * FROM siap.medidor WHERE medi_estado <> 9 and empr_id = {empr_id} ORDER BY medi_id"
        ).on(
            'empr_id -> empr_id
          )
          .as(Medidor._set *)
      }
    }

  /**
    * Crear TipoMedidor
    * @param medidor: Medidor
    */
  def crear(medidor: Medidor): Future[Long] = Future {
    val fecha: LocalDate =
      new LocalDate(Calendar.getInstance().getTimeInMillis())
    val hora: LocalDateTime =
      new LocalDateTime(Calendar.getInstance().getTimeInMillis())
    db.withConnection { implicit connection =>
      val id: Long = SQL("""INSERT INTO siap.medidor (
            medi_numero,
            amem_id,
            amet_id,
            aacu_id,
            empr_id,
            usua_id,
            medi_direccion,
            medi_estado,
            medi_acta) VALUES (
            {medi_numero},
            {amem_id},
            {amet_id},
            {aacu_id},
            {empr_id},
            {usua_id},
            {medi_direccion},
            {medi_estado},
            {medi_acta})""")
        .on(
          'medi_numero -> medidor.medi_numero,
          'amem_id -> medidor.amem_id,
          'amet_id -> medidor.amet_id,
          'aacu_id -> medidor.aacu_id,
          'empr_id -> medidor.empr_id,
          'usua_id -> medidor.usua_id,
          'medi_direccion -> medidor.medi_direccion,
          'medi_estado -> 1,
          'medi_acta -> medidor.medi_acta
        )
        .executeInsert()
        .get
      medidor.datos map { datos =>
        for (d <- datos) {
          val actualizado = SQL(
            """UPDATE siap.medidor_dato SET meda_activa = {meda_activa},
                                                meda_reactiva = {meda_reactiva}, 
                                                meda_nuevo = {meda_nuevo} WHERE
                                                medi_id = {medi_id} and metd_id = {metd_id}"""
          ).on(
              'meda_activa -> d.meda_activa,
              'meda_reactiva -> d.meda_reactiva,
              'meda_nuevo -> d.meda_nuevo,
              'medi_id -> id,
              'metd_id -> d.metd_id
            )
            .executeUpdate() > 0
          val insertado = SQL(
            """INSERT INTO siap.medidor_dato (
                        medi_id, metd_id, meda_activa, meda_reactiva, meda_nuevo
                    ) VALUES ({medi_id}, {metd_id}, {meda_activa}, {meda_reactiva}, {meda_nuevo})"""
          ).on(
              'medi_id -> id,
              'metd_id -> d.metd_id,
              'meda_activa -> d.meda_activa,
              'meda_reactiva -> d.meda_reactiva,
              'meda_nuevo -> d.meda_nuevo
            )
            .executeUpdate() > 0
        }
      }

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> medidor.usua_id,
          'audi_tabla -> "medidor",
          'audi_uid -> id,
          'audi_campo -> "medi_numero",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> medidor.medi_numero,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
    * Actualizar Medidor
    * @param medidor: Medidor
    */
  def actualizar(medidor: Medidor): Boolean = {
    val medidor_ant: Option[Medidor] = buscarPorId(medidor.medi_id.get)
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime =
        new LocalDateTime(Calendar.getInstance().getTimeInMillis())
      val result: Boolean = SQL(
        "UPDATE siap.medidor SET medi_numero = {medi_numero}, amem_id = {amem_id}, amet_id = {amet_id}, aacu_id = {aacu_id}, usua_id = {usua_id}, medi_direccion = {medi_direccion}, medi_estado = {medi_estado}, medi_acta = {medi_acta} WHERE medi_id = {medi_id} and empr_id = {empr_id}"
      ).on(
          'medi_id -> medidor.medi_id,
          'medi_numero -> medidor.medi_numero,
          'amem_id -> medidor.amem_id,
          'amet_id -> medidor.amet_id,
          'aacu_id -> medidor.aacu_id,
          'empr_id -> medidor.empr_id,
          'usua_id -> medidor.usua_id,
          'medi_direccion -> medidor.medi_direccion,
          'medi_estado -> medidor.medi_estado,
          'medi_acta -> medidor.medi_acta
        )
        .executeUpdate() > 0

      medidor.datos map { datos =>
        for (d <- datos) {
          val actualizado = SQL(
            """UPDATE siap.medidor_dato SET meda_activa = {meda_activa},
                                                meda_reactiva = {meda_reactiva}, 
                                                meda_nuevo = {meda_nuevo} WHERE
                                                medi_id = {medi_id} and metd_id = {metd_id}"""
          ).on(
              'meda_activa -> d.meda_activa,
              'meda_reactiva -> d.meda_reactiva,
              'meda_nuevo -> d.meda_nuevo,
              'medi_id -> medidor.medi_id,
              'metd_id -> d.metd_id
            )
            .executeUpdate() > 0
          val insertado = SQL(
            """INSERT INTO siap.medidor_dato (
                        medi_id, metd_id, meda_activa, meda_reactiva, meda_nuevo
                    ) VALUES ({medi_id}, {metd_id}, {meda_activa}, {meda_reactiva}, {meda_nuevo})"""
          ).on(
              'medi_id -> medidor.medi_id,
              'metd_id -> d.metd_id,
              'meda_activa -> d.meda_activa,
              'meda_reactiva -> d.meda_reactiva,
              'meda_nuevo -> d.meda_nuevo
            )
            .executeUpdate() > 0
        }
      }

      if (medidor_ant != None) {
        if (medidor_ant.get.medi_numero != medidor.medi_numero) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> medidor.usua_id,
              'audi_tabla -> "medidor",
              'audi_uid -> medidor.medi_numero,
              'audi_campo -> "medi_numero",
              'audi_valorantiguo -> medidor_ant.get.medi_numero,
              'audi_valornuevo -> medidor.medi_numero,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (medidor_ant.get.amem_id != medidor.amem_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> medidor.usua_id,
              'audi_tabla -> "medidor",
              'audi_uid -> medidor.amem_id,
              'audi_campo -> "amem_id",
              'audi_valorantiguo -> medidor_ant.get.amem_id,
              'audi_valornuevo -> medidor.amem_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (medidor_ant.get.amet_id != medidor.amet_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> medidor.usua_id,
              'audi_tabla -> "medidor",
              'audi_uid -> medidor.amet_id,
              'audi_campo -> "amet_id",
              'audi_valorantiguo -> medidor_ant.get.amet_id,
              'audi_valornuevo -> medidor.amet_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (medidor_ant.get.aacu_id != medidor.aacu_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> medidor.usua_id,
              'audi_tabla -> "medidor",
              'audi_uid -> medidor.aacu_id,
              'audi_campo -> "aacu_id",
              'audi_valorantiguo -> medidor_ant.get.aacu_id,
              'audi_valornuevo -> medidor.aacu_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (medidor_ant.get.usua_id != medidor.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> medidor.usua_id,
              'audi_tabla -> "medidor",
              'audi_uid -> medidor.usua_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> medidor_ant.get.usua_id,
              'audi_valornuevo -> medidor.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (medidor_ant.get.medi_direccion != medidor.medi_direccion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> medidor.usua_id,
              'audi_tabla -> "medidor",
              'audi_uid -> medidor.medi_direccion,
              'audi_campo -> "medi_direccion",
              'audi_valorantiguo -> medidor_ant.get.medi_direccion,
              'audi_valornuevo -> medidor.medi_direccion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (medidor_ant.get.medi_acta != medidor.medi_acta) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
          ).on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> medidor.usua_id,
              'audi_tabla -> "medidor",
              'audi_uid -> medidor.medi_acta,
              'audi_campo -> "medi_acta",
              'audi_valorantiguo -> medidor_ant.get.medi_acta,
              'audi_valornuevo -> medidor.medi_acta,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
      }
      result
    }
  }

  /**
    * Elimnar TipoMedidor
    * @param medidor: TipoMedidor
    */
  def borrar(medi_id: Long, usua_id: Long): Boolean = {
    val fecha: LocalDate = new LocalDate(
      Calendar.getInstance().getTimeInMillis()
    )
    val hora: LocalDateTime = new LocalDateTime(
      Calendar.getInstance().getTimeInMillis()
    )

    db.withConnection { implicit connection =>
      val count: Long =
        SQL("UPDATE siap.medidor SET medi_estado = 9 WHERE medi_id = {medi_id}")
          .on(
            'medi_id -> medi_id
          )
          .executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})"
      ).on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "medidor",
          'audi_uid -> medi_id,
          'audi_campo -> "",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> "",
          'audi_evento -> "E"
        )
        .executeInsert()

      count > 0
    }
  }

  def informe_siap_medidor(empr_id: scala.Long): Future[Iterable[Informe]] =
    Future[Iterable[Informe]] {
      db.withConnection { implicit connection =>
        SQL(
          """SELECT m.medi_id, to_char(m.medi_id, '0000') as medi_codigo, m.medi_numero, m.medi_direccion, ac.aacu_descripcion, COUNT(a.*) AS cantidad FROM siap.medidor m
            LEFT JOIN siap.aap_medidor am ON am.medi_id = m.medi_id AND am.empr_id = m.empr_id
            LEFT JOIN siap.aap_cuentaap ac ON ac.aacu_id = m.aacu_id
            LEFT JOIN siap.aap a ON a.aap_id = am.aap_id and a.empr_id = am.empr_id
            WHERE m.empr_id = {empr_id}
            GROUP BY m.medi_id, medi_codigo, m.medi_numero, m.medi_direccion, ac.aacu_descripcion
            ORDER BY m.medi_numero"""
        ).on('empr_id -> empr_id).as(Informe._set *)
      }
    }

  /*
   *  Carga de Lecturas
   * 
   */
  def cargaLectura(
      a: Int,
      m: Int,
      empr_id: Long,
      usua_id: Long,
      uuid: String
  ): Boolean = {
    val hoy: LocalDateTime = new LocalDateTime(
      Calendar.getInstance().getTimeInMillis()
    )
    val path = System.getProperty("java.io.tmpdir")
    val newTempDir = new File(path, "cargasiap")
    val filenameone =
      "file_medidor_lectura_" + a + "_" + m + "_" + empr_id + ".xlsx"
    val filestringone = newTempDir + "/" + filenameone
    val fileone = new File(filestringone)
    var lastEventTime = Calendar.getInstance().getTime()
    var currentEventTime = Calendar.getInstance().getTime()
    e_id = empr_id
    anho = a
    mes = m
    var exito = true
    var msg = "Carga Exitosa"
    val sessionUUID = UUID.fromString(uuid)
    println("token a usar: " + uuid)
    // val fis = new FileInputStream(fileone)
    // val wb = new HSSFWorkbook(fis)
    var headersLength = 0
    var isHeadersWritten = false
    var filaPos = 0
    val outputPath =
      newTempDir + "/" + "medidor_lectura" + anho + "-" + mes + "_" + empr_id + ".csv"
    val csvWriter = new CSVWriter(outputPath)
    // val wb = WorkbookFactory.create(fileone)
    val fis = new FileInputStream(fileone)
    val wb = new XSSFWorkbook(fis)
    val formulaEvaluator: FormulaEvaluator =
      wb.getCreationHelper().createFormulaEvaluator()
    registerNewStatus(
      new ProcessEvent("0", "0", "1", "medidorPreparingEvent"),
      sessionUUID
    )
    // wb.asScala.zipWithIndex.map {
    val mySheet = wb.getSheetAt(0)
    mySheet match
    {
      case (sheet) =>
        // val sheet = wb.getSheetAt(0)
        println("Inicio Carga: " + sdf.format(Calendar.getInstance().getTime()))
        val totalRow = sheet.getLastRowNum().toString
        println("Filas: " + totalRow)
        // EventStream.publish(new ComenergiaStartLoadingEvent(empr_id, comenergia_id, usua_id, "ComenergiaStartLoadingEvent", "0"))
        val rowIterator = sheet.rowIterator()
        
        while (rowIterator.hasNext) {
          val row = rowIterator.next()
            // registerNewStatus(Status.Parsing, sessionUUID)
            if (Option(row).isDefined) {
              try {
                filaPos += 1
                currentEventTime = Calendar.getInstance().getTime()
                if ((currentEventTime.getTime() - lastEventTime
                      .getTime()) / 1000 >= 1) {
                  registerNewStatus(
                    new ProcessEvent(
                      totalRow.toString(),
                      filaPos.toString(),
                      "2",
                      "medidorParsingEvent"
                    ),
                    sessionUUID
                  )
                  lastEventTime = currentEventTime
                }
                if (filaPos >= 6) {
                  val rowData = (0 until row.getLastCellNum).toArray.map {
                    index =>
                      removeLineBreakingChars(
                        df.formatCellValue(
                          row.getCell(
                            index,
                            Row.MissingCellPolicy.CREATE_NULL_AS_BLANK
                          ),
                          formulaEvaluator
                        )
                      )

                  }
                  // if (isHeadersWritten && filaPos >= 4) {
                  // if (headersLength == rowData.length) {
                  // println(s"escribiendo Fila número ${row.getRowNum}")
                  // csvWriter.write(rowData)
                  escribirFilaOne(rowData, e_id)
                  // } else {
                  // println(s"Fila invalida [Fila no - ${row.getRowNum}] [Headers count = $headersLength and current row columns count = ${rowData.length}]")
                  // }
                } // else if (!isHeadersWritten) {
                // csvWriter.writeHeaders(rowData)
                // isHeadersWritten = true
                // headersLength = rowData.length
                // }
              } catch {
                case NonFatal(th) =>
                  msg = th.getMessage() + ", [Linea " + (filaPos) + "]"
                  exito = false
                  th.printStackTrace()
              }
            }
        }
        // registerNewStatus(Status.Done, sessionUUID)
        registerNewStatus(
          new ProcessEvent("0", "0", "3", "medidorDoneEvent"),
          sessionUUID
        )
        println("Fin Carga: " + sdf.format(Calendar.getInstance().getTime()))
    }
    db.withConnection { implicit connection =>
      SQL("""INSERT INTO siap.controlcarga 
                    (
                        controlcarga_anho, 
                        controlcarga_mes,
                        controlcarga_fecha,
                        usua_id,
                        empr_id,
                        controlcarga_registros,
                        controlcarga_tipo,
                        controlcarga_exito,
                        controlcarga_mensaje,
                        controlcarga_estado
                    ) VALUES (
                        {controlcarga_anho}, 
                        {controlcarga_mes},
                        {controlcarga_fecha},
                        {usua_id},
                        {empr_id},
                        {controlcarga_registros},
                        {controlcarga_tipo},
                        {controlcarga_exito},
                        {controlcarga_mensaje},
                        {controlcarga_estado}
                    )""")
        .on(
          'controlcarga_anho -> anho,
          'controlcarga_mes -> mes,
          'controlcarga_fecha -> hoy,
          'usua_id -> usua_id,
          'empr_id -> e_id,
          'controlcarga_registros -> (filaPos - 5),
          'controlcarga_tipo -> 1,
          'controlcarga_exito -> exito,
          'controlcarga_mensaje -> msg,
          'controlcarga_estado -> 1
        )
        .executeUpdate()
    }
    true
  }

  def escribirFilaOne(fila: Array[String], empr_id: Long): Unit = {
    val medi_id = {
      val medi_numero = fila(3).replaceAll("[a-zA-Z\\-]{0,}","") 
      val medidor = buscarPorComenergia(fila(3).replaceAll("[a-zA-Z\\-]{0,}",""))
      medidor match {
        case Some(m) =>
          m.medi_id match {
            case Some(id) => id
            case None     => 0
          }
        case None => 0
      }
    }
    val mele_cuenta = (fila(1) match {
      case "" => ""
      case v => v
    })
    val mele_nombre = (fila(2) match {
      case "" => ""
      case v => v
    })
    val mele_comenergia = (fila(3) match {
      case "" => ""
      case v => v
    })
    val mele_activa_anterior = (fila(4) match {
      case "" => "0"
      case v  => v
    }).toInt
    val mele_activa_actual = (fila(5) match {
      case "" => "0"
      case v  => v
    }).toInt
    val mele_reactiva = (fila(6) match {
      case "" => "0"
      case v  => v
    }).toInt
    val mele_reactiva_anterior = (fila(7) match {
      case "" => "0"
      case v  => v
    }).toInt
    val mele_reactiva_actual = (fila(8) match {
      case "" => "0"
      case v  => v
    }).toInt
    db.withConnection { implicit connection =>
      SQL("""INSERT INTO siap.medidor_lectura (
                    medi_id,
                    mele_anho,
                    mele_mes,
                    mele_activa_anterior,
                    mele_activa_actual,
                    mele_reactiva,
                    mele_reactiva_anterior,
                    mele_reactiva_actual,
                    mele_comenergia,
                    mele_cuenta,
                    mele_nombre,
                    empr_id
                ) VALUES (
                    {medi_id},
                    {mele_anho},
                    {mele_mes},
                    {mele_activa_anterior},
                    {mele_activa_actual},
                    {mele_reactiva},
                    {mele_reactiva_anterior},
                    {mele_reactiva_actual},   
                    {mele_comenergia},
                    {mele_cuenta},
                    {mele_nombre},
                    {empr_id}
                )""")
        .on(
            'medi_id -> medi_id,
            'mele_anho -> anho,
            'mele_mes -> mes,
            'mele_activa_anterior -> mele_activa_anterior,
            'mele_activa_actual -> mele_activa_actual,
            'mele_reactiva -> mele_reactiva,
            'mele_reactiva_anterior -> mele_reactiva_anterior,
            'mele_reactiva_actual -> mele_reactiva_actual,
            'mele_comenergia -> mele_comenergia,
            'mele_cuenta -> mele_cuenta,
            'mele_nombre -> mele_nombre,
            'empr_id -> empr_id
        )
        .executeUpdate()
    }
  }

class CSVWriter(path: String) {

    private val writer = new BufferedWriter(
      (new OutputStreamWriter((new FileOutputStream(path))))
    )

    private val csvSettings = new CsvWriterSettings()
    csvSettings.getFormat.setDelimiter('\t')
    csvSettings.getFormat.setQuoteEscape('\\')
    csvSettings.setQuoteAllFields(true)

    private val csvWriter = new CsvWriter(writer, csvSettings)

    def writeHeaders(headers: Array[String]): Unit =
      csvWriter.writeHeaders(headers: _*)

    def write(row: Array[String]): Unit =
      csvWriter.writeRow(row)

    def close(): Unit = {
      csvWriter.flush()
      csvWriter.close()
    }
  }

  def removeLineBreakingChars(cell: String): String =
    cell.replaceAll("[\\t\\n\\r]", " ")

}
