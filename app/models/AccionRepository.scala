package models

import javax.inject.Inject
import java.util.Calendar

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

case class Accion(acci_id: Option[Long],
                  acci_descripcion: String,
                  acci_enaap: Option[Boolean],
                  acci_estado: Int,
                  usua_id: Long)

object Accion {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val accionWrites = new Writes[Accion] {
    def writes(accion: Accion) = Json.obj(
      "acci_id" -> accion.acci_id,
      "acci_descripcion" -> accion.acci_descripcion,
      "acci_enaap" -> accion.acci_enaap,
      "acci_estado" -> accion.acci_estado,
      "usua_id" -> accion.usua_id      
    )
  }

  implicit val accionReads: Reads[Accion] = (
    (__ \ "acci_id").readNullable[Long] and
      (__ \ "acci_descripcion").read[String] and
      (__ \ "acci_enaap").readNullable[Boolean] and
      (__ \ "acci_estado").read[Int] and
      (__ \ "usua_id").read[Long]      
  )(Accion.apply _)
}

class AccionRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    * Parsear un Accion desde un ResultSet
    */
  private val simple = {
    get[Option[Long]]("accion.acci_id") ~
      get[String]("accion.acci_descripcion") ~
      get[Option[Boolean]]("accion.acci_enaap") ~
      get[Int]("accion.acci_estado") ~ 
      get[Long]("accion.usua_id") map {
      case acci_id ~ acci_descripcion ~ acci_enaap ~ acci_estado ~ usua_id  =>
        Accion(acci_id, acci_descripcion, acci_enaap, acci_estado, usua_id)
    }
  }

  /**
    * Recuperar una accion por su acci_id
    */
  def buscarPorId(acci_id: Long): Option[Accion] = {
    db.withConnection { implicit connection =>
      SQL(
        "SELECT * FROM siap.accion WHERE acci_id = { acci_id }")
        .on(
          'acci_id -> acci_id
        )
        .as(simple.singleOpt)
    }
  }

  /**
    * Recuperar una accion por su acci_descripcion
    */
  def buscarPorDescripcion(acci_descripcion: String): Future[Iterable[Accion]] =
    Future[Iterable[Accion]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.accion WHERE acci_descripcion like %{acci_descripcion}% ORDER BY acci_descripcion")
          .on(
            'acci_descripcion -> acci_descripcion
          )
          .as(simple *)
      }
    }
   /**
   * Recuperar total de registros
   * @return total
   */
   def cuenta(): Long =  {
     db.withConnection{ implicit connection =>
       val result = SQL("SELECT COUNT(*) AS c FROM siap.accion WHERE acci_estado = 1").as(SqlParser.scalar[Long].single)
       result
     }
   }

  /**
    * Recuperar todas las accion
    */
  def todos(page_size: Long, current_page: Long): Future[Iterable[Accion]] = Future[Iterable[Accion]] {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.accion WHERE acci_estado= 1 LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)").
      on(
        'page_size -> page_size,
        'current_page -> current_page
      )
      .as(simple *)
    }
  }

    /**
    * Recuperar todas las accion
    */
  def acciones(): Future[Iterable[Accion]] = Future[Iterable[Accion]] {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.accion WHERE acci_estado= 1").as(simple *)
    }
  }

  /**
    * Crear una accion
    */
  def crear(accion: Accion): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val id: Long = SQL(
        "INSERT INTO siap.accion (acci_descripcion, acci_enaap, usua_id) VALUES ({acci_descripcion},{acci_enaap},{usua_id}")
        .on(
          "acci_descripcion" -> accion.acci_descripcion,
          "acci_enaap" -> accion.acci_enaap,
          "usua_id" -> accion.usua_id
        )
        .executeInsert()
        .get

      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> accion.usua_id,
          'audi_tabla -> "accion",
          'audi_uid -> id,
          'audi_campo -> "acci_descripcion",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> accion.acci_descripcion,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
    * Actualizar Accion
    */
  def actualizar(accion: Accion): Boolean = {
    val accion_ant: Option[Accion] = buscarPorId(accion.acci_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val result: Boolean = SQL(
        "UPDATE siap.accion SET acci_descripcion = {acci_descripcion}, acci_enaap = {acci_enaap} WHERE acci_id = {acci_id}")
        .on(
          'acci_descripcion -> accion.acci_descripcion,
          'acci_enaap -> accion.acci_enaap,
          'usua_id -> accion.usua_id
        )
        .executeUpdate() > 0

      if (accion_ant != None) {
        if (accion_ant.get.acci_descripcion != accion.acci_descripcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> accion.usua_id,
              'audi_tabla -> "accion",
              'audi_uid -> accion.acci_id,
              'audi_campo -> "acci_descripcion",
              'audi_valorantiguo -> accion_ant.get.acci_descripcion,
              'audi_valornuevo -> accion.acci_descripcion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (accion_ant.get.acci_enaap != accion.acci_enaap) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> accion.usua_id,
              'audi_tabla -> "accion",
              'audi_uid -> accion.acci_id,
              'audi_campo -> "acci_enaap",
              'audi_valorantiguo -> accion_ant.get.acci_enaap,
              'audi_valornuevo -> accion.acci_enaap,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (accion_ant.get.usua_id != accion.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> accion.usua_id,
              'audi_tabla -> "accion",
              'audi_uid -> accion.acci_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> accion_ant.get.usua_id,
              'audi_valornuevo -> accion.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
      }
      result
    }
  }

  /**
    * Eliminar Accion
    */
  def borrar(acci_id: Long, usua_id: Long): Boolean = {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha

      val count: Long =
        SQL("UPDATE siap.accion SET acci_estado = 9 WHERE acci_id = {acci_id}")
          .on(
            'acci_id -> acci_id
          )
          .executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "accion",
          'audi_uid -> acci_id,
          'audi_campo -> "",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> "",
          'audi_evento -> "E"
        )
        .executeInsert()

      count > 0
    }
  }
}
