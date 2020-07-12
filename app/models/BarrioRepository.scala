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

case class Barrio(barr_id: Option[Long],
                  barr_descripcion: String,
                  barr_codigo: String,
                  barr_estado: Int,
                  depa_id: Long,
                  muni_id: Long,
                  tiba_id: Long,
                  usua_id: Long)

object Barrio {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val barrioWrites = new Writes[Barrio] {
    def writes(barrio: Barrio) = Json.obj(
      "barr_id" -> barrio.barr_id,
      "barr_descripcion" -> barrio.barr_descripcion,
      "barr_codigo" -> barrio.barr_codigo,
      "barr_estado" -> barrio.barr_estado,
      "depa_id" -> barrio.depa_id,
      "muni_id" -> barrio.muni_id,
      "tiba_id" -> barrio.tiba_id,
      "usua_id" -> barrio.usua_id
    )
  }

  implicit val barrioReads: Reads[Barrio] = (
    (__ \ "barr_id").readNullable[Long] and
      (__ \ "barr_descripcion").read[String] and
      (__ \ "barr_codigo").read[String] and
      (__ \ "barr_estado").read[Int] and      
      (__ \ "depa_id").read[Long] and
      (__ \ "muni_id").read[Long] and
      (__ \ "tiba_id").read[Long] and
      (__ \ "usua_id").read[Long]
  )(Barrio.apply _)
}

class BarrioRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    * Parsear un Barrio desde un ResultSet
    */
  private val simple = {
    get[Option[Long]]("barrio.barr_id") ~
      get[String]("barrio.barr_descripcion") ~
      get[String]("barrio.barr_codigo") ~
      get[Int]("barrio.barr_estado") ~
      get[Long]("barrio.depa_id") ~
      get[Long]("barrio.muni_id") ~
      get[Long]("barrio.tiba_id") ~
      get[Long]("barrio.usua_id") map {
      case barr_id ~ barr_descripcion ~ barr_codigo ~ barr_estado ~ depa_id ~ muni_id ~ tiba_id ~ usua_id =>
        Barrio(barr_id,
               barr_descripcion,
               barr_codigo,
               barr_estado,
               depa_id,
               muni_id,
               tiba_id,
               usua_id
               )
    }
  }

  /**
    * Recuperar un Barrio por su barr_id
    */
  def buscarPorId(barr_id: Long): Option[Barrio] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.barrio WHERE barr_id = {barr_id} and barr_estado <> 9")
        .on(
          'barr_id -> barr_id
        )
        .as(simple.singleOpt)
    }
  }

  /**
    * Recuperar un Barrio por su barr_descripcion
    */
  def buscarPorDescripcion(barr_descripcion: String,
                           muni_id: Long): Future[Iterable[Barrio]] =
    Future[Iterable[Barrio]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.barrio WHERE muni_id = {muni_id} and barr_descripcion like %{barr_descripcion}% ORDER BY barr_descripcion")
          .on(
            'muni_id -> muni_id,
            'barr_descripcion -> barr_descripcion
          )
          .as(simple *)
      }
    }

  /**
    * Recuperar un Barrio por su muni_id
    */
  def buscarPorMunicipio(muni_id: Long): Future[Iterable[Barrio]] =
    Future[Iterable[Barrio]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.barrio WHERE muni_id = {muni_id} and barr_estado <> 9 ORDER BY barr_descripcion")
          .on(
            'muni_id -> muni_id
          )
          .as(simple *)
      }
    }

  /**
  * Recuperar total de registros
  * @return total
  */
  def cuenta(muni_id: Long): Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.barrio WHERE muni_id = {muni_id} AND barr_estado <> 9").
      on(
        'muni_id -> muni_id
      ).as(SqlParser.scalar[Long].single)
      result
    }
  }

  /**
    * Recuperar todos los barrios de un municipio
    */
  def todos(muni_id: Long, page_size: Long, current_page: Long): Future[Iterable[Barrio]] =
    Future[Iterable[Barrio]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.barrio where muni_id = {muni_id} AND barr_estado <> 9 ORDER BY barr_descripcion, barr_codigo LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)")
          .on(
            'muni_id -> muni_id,
            'page_size -> page_size,
            'current_page -> current_page
          )
          .as(simple *)
      }
    }

  /**
    * Crear un Barrio
    */
  def crear(barrio: Barrio): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val id: Long = SQL(
        "INSERT INTO siap.barrio (barr_descripcion, barr_codigo, barr_estado, depa_id, muni_id, tiba_id, usua_id) VALUES ({barr_descripcion}, {barr_codigo}, {barr_estado}, {depa_id}, {muni_id}, {tiba_id}, {usua_id})")
        .on(
          'barr_descripcion -> barrio.barr_descripcion,
          'barr_codigo -> barrio.barr_codigo,
          'barr_estado -> barrio.barr_estado,
          'depa_id -> barrio.depa_id,
          'muni_id -> barrio.muni_id,
          'tiba_id -> barrio.tiba_id,
          'usua_id -> barrio.usua_id
        )
        .executeInsert()
        .get

      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> barrio.usua_id,
          'audi_tabla -> "barrio",
          'audi_uid -> id,
          'audi_campo -> "barr_descripcion",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> barrio.barr_descripcion,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
    * Actualizar Barrio
    */
  def actualizar(barrio: Barrio): Boolean = {
    val barrio_ant: Option[Barrio] = buscarPorId(barrio.barr_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val result: Boolean = SQL(
        "UPDATE siap.barrio SET barr_descripcion = {barr_descripcion}, barr_codigo = {barr_codigo}, depa_id = {depa_id}, muni_id = {muni_id}, tiba_id = {tiba_id}, usua_id = {usua_id} WHERE barr_id = {barr_id}")
        .on(
          'barr_descripcion -> barrio.barr_descripcion,
          'barr_codigo -> barrio.barr_codigo,
          'depa_id -> barrio.depa_id,
          'muni_id -> barrio.muni_id,
          'tiba_id -> barrio.tiba_id,
          'usua_id -> barrio.usua_id,
          'barr_id -> barrio.barr_id
        )
        .executeUpdate() > 0

      if (barrio_ant != None) {
        if (barrio_ant.get.barr_descripcion != barrio.barr_descripcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> barrio.usua_id,
              'audi_tabla -> "barrio",
              'audi_uid -> barrio.barr_id,
              'audi_campo -> "barr_descripcion",
              'audi_valorantiguo -> barrio_ant.get.barr_descripcion,
              'audi_valornuevo -> barrio.barr_descripcion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (barrio_ant.get.barr_codigo != barrio.barr_codigo) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> barrio.usua_id,
              'audi_tabla -> "barrio",
              'audi_uid -> barrio.barr_id,
              'audi_campo -> "barr_codigo",
              'audi_valorantiguo -> barrio_ant.get.barr_codigo,
              'audi_valornuevo -> barrio.barr_codigo,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (barrio_ant.get.muni_id != barrio.muni_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> barrio.usua_id,
              'audi_tabla -> "barrio",
              'audi_uid -> barrio.barr_id,
              'audi_campo -> "muni_id",
              'audi_valorantiguo -> barrio_ant.get.muni_id,
              'audi_valornuevo -> barrio.muni_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (barrio_ant.get.tiba_id != barrio.tiba_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> barrio.usua_id,
              'audi_tabla -> "barrio",
              'audi_uid -> barrio.barr_id,
              'audi_campo -> "tiba_id",
              'audi_valorantiguo -> barrio_ant.get.tiba_id,
              'audi_valornuevo -> barrio.tiba_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (barrio_ant.get.usua_id != barrio.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> barrio.usua_id,
              'audi_tabla -> "barrio",
              'audi_uid -> barrio.barr_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> barrio_ant.get.usua_id,
              'audi_valornuevo -> barrio.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
      }
      result
    }
  }

  /**
    * Eliminar Barrio
    */
  def borrar(barr_id: Long): Boolean = {
    val barrio_ant: Option[Barrio] = buscarPorId(barr_id)
    val barrio = barrio_ant.get    
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha

      val count: Long =
        SQL("UPDATE siap.barrio SET barr_estado = 9 WHERE barr_id = {barr_id}")
          .on(
            'barr_id -> barrio.barr_id
          )
          .executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> barrio.usua_id,
          'audi_tabla -> "barrio",
          'audi_uid -> barrio.barr_id,
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
