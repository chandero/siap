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

case class MedioAmbiente(meam_id: Option[Long],
                         meam_descripcion: String,
                         meam_estado: Int,
                         usua_id: Long)

object MedioAmbiente {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val medioambienteWrites = new Writes[MedioAmbiente] {
    def writes(medioambiente: MedioAmbiente) = Json.obj(
      "meam_id" -> medioambiente.meam_id,
      "meam_descripcion" -> medioambiente.meam_descripcion,
      "meam_estado" -> medioambiente.meam_estado,
      "usua_id" -> medioambiente.usua_id
    )
  }

  implicit val medioambienteReads: Reads[MedioAmbiente] = (
    (__ \ "meam_id").readNullable[Long] and
      (__ \ "meam_descripcion").read[String] and
      (__ \ "meam_estado").read[Int] and
      (__ \ "usua_id").read[Long]
  )(MedioAmbiente.apply _)
}

class MedioAmbienteRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
        Parsear un MedioAmbiente desde un ResultSet
    */
  private val simple = {
    get[Option[Long]]("medioambiente.meam_id") ~
      get[String]("medioambiente.meam_descripcion") ~
      get[Int]("medioambiente.meam_estado") ~
      get[Long]("medioambiente.usua_id") map {
      case meam_id ~ meam_descripcion ~ meam_estado ~ usua_id =>
        MedioAmbiente(meam_id, meam_descripcion, meam_estado, usua_id)
    }
  }

  /**
  * Recuperar todos los MedioAmbiente
  */
  def todos():Future[Iterable[MedioAmbiente]] = Future[Iterable[MedioAmbiente]] {
      db.withConnection { implicit connection => 
          SQL("SELECT * FROM siap.medioambiente ORDER BY meam_id").
          as(simple *)            
      }
  }  

  /**
        Recuperar un MedioAmbiente por su meam_id
        @param meam_id: Long
    */
  def buscarPorId(meam_id: Long): Option[MedioAmbiente] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.medioambiente WHERE meam_id = {meam_id}")
        .on(
          'meam_id -> meam_id
        )
        .as(simple.singleOpt)
    }
  }

  /**
        Recuperar medioambiente por su meam_descripcion
    */
  def buscarPorDescripcion(
      meam_descripcion: String): Future[Iterable[MedioAmbiente]] =
    Future[Iterable[MedioAmbiente]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.medioambiente WHERE meam_descripcion LIKE %{meam_descripcion}% ORDER BY meam_descripcion")
          .on(
            'meam_descripcion -> meam_descripcion
          )
          .as(simple *)
      }
    }

  /**
        Crear MedioAmbiente
        @param meam: MedioAmbiente
    */
  def crear(meam: MedioAmbiente): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val id: Long = SQL(
        "INSERT INTO siap.medioambiente (meam_descripcion, meam_estado, usua_id) VALUES ({meam_descripcion}, {meam_estado}, {usua_id})")
        .on(
          "meam_descripcion" -> meam.meam_descripcion,
          "meam_estado" -> meam.meam_estado,
          "usua_id" -> meam.usua_id
        )
        .executeInsert()
        .get

      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> meam.usua_id,
          'audi_tabla -> "medioambiente",
          'audi_uid -> id,
          'audi_campo -> "meam_descripcion",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> meam.meam_descripcion,
          'audi_evento -> "I"
        )
        .executeInsert()
      id
    }
  }

  /**
    Actualizar MedioAmbiente
    @param meam: MedioAmbiente
    */
  def actualizar(meam: MedioAmbiente): Long = {
    val meam_ant: Option[MedioAmbiente] = buscarPorId(meam.meam_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val count: Long = SQL(
        "UPDATE siap.medioambiente SET meam_descripcion = {meam_descripcion}, meam_estado = {meam_estado}, usua_id = {usua_id} WHERE meam_id = {meam_id}")
        .on(
          "meam_id" -> meam.meam_id,
          "meam_descripcion" -> meam.meam_descripcion,
          "meam_estado" -> meam.meam_estado,
          "usua_id" -> meam.usua_id
        )
        .executeUpdate()

      if (meam_ant != None) {
        if (meam_ant.get.meam_descripcion != meam.meam_descripcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> meam.usua_id,
              'audi_tabla -> "medioambiente",
              'audi_uid -> meam.meam_id,
              'audi_campo -> "meam_descripcion",
              'audi_valorantiguo -> meam_ant.get.meam_descripcion,
              'audi_valornuevo -> meam.meam_descripcion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (meam_ant.get.meam_estado != meam.meam_estado) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> meam.usua_id,
              'audi_tabla -> "medioambiente",
              'audi_uid -> meam.meam_id,
              'audi_campo -> "meam_estado",
              'audi_valorantiguo -> meam_ant.get.meam_estado,
              'audi_valornuevo -> meam.meam_estado,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (meam_ant.get.usua_id != meam.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> meam.usua_id,
              'audi_tabla -> "medioambiente",
              'audi_uid -> meam.meam_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> meam_ant.get.usua_id,
              'audi_valornuevo -> meam.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }        
      }

      count
    }
  }

  /**
    Eliminar MedioAmbiente
    @param meam: MedioAmbiente
   */
   def eliminar(meam: MedioAmbiente): Boolean = {
       db.withConnection { implicit connection =>
           val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
            val hora: LocalDate = fecha        

            val count:Long = SQL("UPDATE siap.medioambiente SET meam_estado = 9 WHERE meam_id = {meam_id}").
            on(
                'meam_id -> meam.meam_id
            ).executeUpdate

            SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento, audi_alias) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento}, {audi_alias})").
                on(
                    'audi_fecha -> fecha,
                    'audi_hora -> hora,
                    'usua_id -> meam.usua_id,
                    'audi_tabla -> "medioambiente", 
                    'audi_uid -> meam.meam_id,
                    'audi_campo -> "", 
                    'audi_valorantiguo -> "",
                    'audi_valornuevo -> "",
                    'audi_evento -> "E").
                    executeInsert()       
            count > 0
       }
   }

}
