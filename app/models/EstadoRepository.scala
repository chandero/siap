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

case class Estado(esta_id: Option[Long],
                  esta_descripcion: String,
                  esta_esactivo: Boolean,
                  esta_estado: Int,
                  usua_id: Long)

object Estado {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val estadoWrites = new Writes[Estado] {
    def writes(estado: Estado) = Json.obj(
      "esta_id" -> estado.esta_id,
      "esta_descripcion" -> estado.esta_descripcion,
      "esta_esactivo" -> estado.esta_esactivo,
      "esta_estado" -> estado.esta_estado,
      "usua_id" -> estado.usua_id
    )
  }

  implicit val estadoReads: Reads[Estado] = (
    (__ \ "esta_id").readNullable[Long] and
      (__ \ "esta_descripcion").read[String] and
      (__ \ "esta_esactivo").read[Boolean] and
      (__ \ "esta_estado").read[Int] and
      (__ \ "usua_id").read[Long]
  )(Estado.apply _)
}

class EstadoRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    Parsear un Estado desde un ResultSet
    */
  private val simple = {
    get[Option[Long]]("estado.esta_id") ~
      get[String]("estado.esta_descripcion") ~
      get[Boolean]("estado.esta_esactivo") ~
      get[Int]("estado.esta_estado") ~
      get[Long]("estado.usua_id") map {
      case esta_id ~ esta_descripcion ~ esta_esactivo ~ esta_estado ~ usua_id =>
        Estado(esta_id, esta_descripcion, esta_esactivo, esta_estado, usua_id)
    }
  }

  /**
    Recuperar un Estado por su esta_id
    @param esta_id: Long
    */
  def buscarPorId(esta_id: Long): Option[Estado] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.estado WHERE esta_id = {esta_id}")
        .on(
          'esta_id -> esta_id
        )
        .as(simple.singleOpt)
    }
  }

  /**
    Recuperar Estado por esta_descripcion
    @param esta_descripcion: String
    */
  def buscarPorDescripcion(esta_descripcion: String): Future[Iterable[Estado]] =
    Future[Iterable[Estado]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.estado WHERE esta_descripcion LIKE %{esta_descripcion}% ORDER BY esta_descripcion")
          .on(
            'esta_descripcion -> esta_descripcion
          )
          .as(simple *)
      }
    }

  /**
    Recuperar todos los Estados
    */
  def todos(): Future[Iterable[Estado]] = Future[Iterable[Estado]] {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.estado").as(simple *)
    }
  }

  /**
    Crear Estado
    @param estado: Estado
    */
  def crear(estado: Estado): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val id: Long = SQL(
        "INSERT INTO siap.estado (esta_descripcion, esta_esactivo, esta_estado, usua_id) VALUES ({esta_descripcion},{esta_esactivo},{esta_estado},{usua_id})")
        .on(
          'esta_descripcion -> estado.esta_descripcion,
          'esta_esactivo -> estado.esta_esactivo,
          'esta_estado -> estado.esta_estado,
          'usua_id -> estado.usua_id
        )
        .executeInsert()
        .get

      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> estado.usua_id,
          'audi_tabla -> "estado",
          'audi_uid -> id,
          'audi_campo -> "esta_descripcion",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> estado.esta_descripcion,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
    Actualizar Estado
    @param estado: Estado
    */
  def actualizar(estado: Estado): Boolean = {
    val estado_ant: Option[Estado] = buscarPorId(estado.esta_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val result: Boolean = SQL(
        "UPDATE siap.estado SET esta_descripcion = {esta_descripcion}, esta_esactivo = {esta_esactivo}, esta_estado = {esta_estado}, usua_id = {usua_id} WHERE esta_id = {esta_id}")
        .on(
          'esta_descripcion -> estado.esta_descripcion,
          'esta_esactivo -> estado.esta_esactivo,
          'esta_estado -> estado.esta_estado,
          'usua_id -> estado.usua_id
        )
        .executeUpdate() > 0

      if (estado_ant != None) {
        if (estado_ant.get.esta_descripcion != estado.esta_descripcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> estado.usua_id,
              'audi_tabla -> "estado",
              'audi_uid -> estado.esta_id,
              'audi_campo -> "esta_descripcion",
              'audi_valorantiguo -> estado_ant.get.esta_descripcion,
              'audi_valornuevo -> estado.esta_descripcion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (estado_ant.get.esta_esactivo != estado.esta_esactivo) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> estado.usua_id,
              'audi_tabla -> "estado",
              'audi_uid -> estado.esta_id,
              'audi_campo -> "esta_esactivo",
              'audi_valorantiguo -> estado_ant.get.esta_esactivo,
              'audi_valornuevo -> estado.esta_esactivo,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (estado_ant.get.esta_estado != estado.esta_estado) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> estado.usua_id,
              'audi_tabla -> "estado",
              'audi_uid -> estado.esta_id,
              'audi_campo -> "esta_estado",
              'audi_valorantiguo -> estado_ant.get.esta_estado,
              'audi_valornuevo -> estado.esta_estado,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (estado_ant.get.usua_id != estado.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> estado.usua_id,
              'audi_tabla -> "estado",
              'audi_uid -> estado.esta_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> estado_ant.get.usua_id,
              'audi_valornuevo -> estado.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        
      }
      result
    }
  }

  /**
    Eliminar Estado
    @param estado: Estado
  */
  def eliminar(estado: Estado) : Boolean = {
     db.withConnection { implicit connection => 
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha

      val count: Long =
        SQL("UPDATE siap.estado SET esta_estado = 9 WHERE esta_id = {esta_id}").
        on(
            'esta_id -> estado.esta_id
        ).executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> estado.usua_id,
          'audi_tabla -> "estado",
          'audi_uid -> estado.esta_id,
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
