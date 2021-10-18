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
import scala.collection.mutable.ListBuffer

import org.joda.time.DateTime
import org.joda.time.LocalDate
import org.joda.time.LocalDateTime

case class UcapControl(ucco_id: Option[Long],
                       empr_id: Option[Long],
                       ucco_codigo: Option[Long],
                       ucco_direccion: Option[String],
                       barr_id: Option[Long],
                       usua_id: Option[Long],
                       ucco_estado: Option[Int])

object UcapControl {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val Writes = new Writes[UcapControl] {
    def writes(u: UcapControl) = Json.obj(
      "ucco_id" -> u.ucco_id,
      "empr_id" -> u.empr_id,
      "ucco_codigo" -> u.ucco_codigo,
      "ucco_direccion" -> u.ucco_direccion,
      "barr_id" -> u.barr_id,
      "usua_id" -> u.usua_id,
      "ucco_estado" -> u.ucco_estado
    )
  }

  implicit val Reads: Reads[UcapControl] = (
    (__ \ "ucco_id").readNullable[Long] and
    (__ \ "empr_id").readNullable[Long] and
    (__ \ "ucco_codigo").readNullable[Long] and
    (__ \ "ucco_direccion").readNullable[String] and
    (__ \ "barr_id").readNullable[Long] and
    (__ \ "usua_id").readNullable[Long] and
    (__ \ "ucco_estado").readNullable[Int]
  )(UcapControl.apply _)

  val _set = {
    get[Option[Long]]("ucco_id") ~
      get[Option[Long]]("empr_id") ~
      get[Option[Long]]("ucco_codigo") ~
      get[Option[String]]("ucco_direccion") ~
      get[Option[Long]]("barr_id") ~
      get[Option[Long]]("usua_id") ~
      get[Option[Int]]("uuco_estado") map {
      case ucco_id ~ empr_id ~ ucco_codigo ~ ucco_direccion ~ barr_id ~ usua_id ~ uuco_estado =>
        UcapControl(ucco_id,
                    empr_id,
                    ucco_codigo,
                    ucco_direccion,
                    barr_id,
                    usua_id,
                    uuco_estado)
      }  
    }  
}

class UcapControlRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    Recuperar un UcapControl por su ucco_id
    */
  def buscarPorId(id: Long): Option[UcapControl] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.ucap_control WHERE ucco_id = {id} and ucco_estado <> 9")
        .on(
          'ucco_id -> id
        )
        .as(UcapControl._set.singleOpt)
    }
  }

  /**
    Recuperar por Codigo
    */
    def buscarPorCodigo(codigo: Long, empr_id: Long): Option[UcapControl] = {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.ucap_control WHERE ucco_codigo = {codigo} and empr_id = {empr_id} and ucco_estado <> 9")
          .on(
            'codigo -> codigo,
            'empr_id -> empr_id
          )
          .as(UcapControl._set.singleOpt)
      }
    }
    
    
  /**
  * Recuperar total de registros
  * @param empr_id: Long
  * @return total
  */
  def cuenta(empr_id: Long): Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.ucap_control WHERE empr_id = {empr_id} and ucco_estado <> 9").
      on(
        'empr_id -> empr_id,
      ).as(SqlParser.scalar[Long].single)
      result
    }
  }    

  /**
    Recuperar todos los Registros de una empresa
    @param empr_id: Long
    */
  def todos(page_size:Long, current_page:Long, empr_id: Long): Future[Iterable[UcapControl]] =
    Future[Iterable[UcapControl]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.ucap_control WHERE empr_id = {empr_id} and ucco_estado <> 9 ORDER BY ucco_codigo ASC LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1)")
          .on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page,
          )
          .as(UcapControl._set *)
      }
    }


  /**
  * Recuperar todos los Unidad activas
  */
  def ucap_control(empr_id:Long): Future[Iterable[UcapControl]] = Future[Iterable[UcapControl]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.ucap_control WHERE empr_id = {empr_id} and ucco_estado <> 9 ORDER BY ucco_codigo")
          .on(
            'empr_id -> empr_id,
          )
          .as(UcapControl._set *)
      }
    }    

  /**
        Crear un Registro
        @param e: Object
    */
  def crear(u: UcapControl): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
      val id: Long = SQL(
        "INSERT INTO siap.ucap_control (empr_id, ucco_codigo, ucco_direccion, barr_id, usua_id, ucco_estado) VALUES ({empr_id}, {ucco_codigo}, {ucco_direccion}, {barr_id}, {usua_id}, {ucco_estado})")
        .on(
          'empr_id -> u.empr_id, 
          'ucco_codigo -> u.ucco_codigo, 
          'ucco_direccion -> u.ucco_direccion, 
          'barr_id -> u.barr_id, 
          'usua_id -> u.usua_id, 
          'ucco_estado-> 1
        )
        .executeInsert()
        .get

      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> u.usua_id,
          'audi_tabla -> "ucap_control",
          'audi_uid -> id,
          'audi_campo -> "ucco_codigo",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> u.ucco_codigo,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
    Actualizar Registro
    @param e: Object
    */
  def actualizar(e: UcapControl): Boolean = {
    val e_ant: Option[UcapControl] = buscarPorId(e.ucco_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        
      val result: Boolean = SQL(
        "UPDATE siap.ucap_control SET ucco_direccion = {ucco_direccion}, ucco_estado = {ucco_estado}, barr_id = {barr_id}, empr_id = {empr_id}, usua_id = {usua_id} WHERE ucco_id = {ucco_id}")
        .on(
          'ucco_direccion -> e.ucco_direccion,
          'barr_id -> e.barr_id,
          'ucco_estado -> e.ucco_estado,
          'empr_id -> e.empr_id,
          'usua_id -> e.usua_id,
          'ucco_id -> e.ucco_id
        )
        .executeUpdate() > 0

      if (e_ant != None) {
        if (e_ant.get.ucco_direccion != e.ucco_direccion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> e.usua_id,
              'audi_tabla -> "ucap_control",
              'audi_uid -> e.ucco_id,
              'audi_campo -> "ucco_direccion",
              'audi_valorantiguo -> e_ant.get.ucco_direccion,
              'audi_valornuevo -> e.ucco_direccion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (e_ant.get.barr_id != e.barr_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> e.usua_id,
              'audi_tabla -> "ucap_control",
              'audi_uid -> e.ucco_id,
              'audi_campo -> "barr_id",
              'audi_valorantiguo -> e_ant.get.barr_id,
              'audi_valornuevo -> e.barr_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
        if (e_ant.get.ucco_estado != e.ucco_estado) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> e.usua_id,
              'audi_tabla -> "ucap_control",
              'audi_uid -> e.ucco_id,
              'audi_campo -> "ucco_estado",
              'audi_valorantiguo -> e_ant.get.ucco_estado,
              'audi_valornuevo -> e.ucco_estado,
              'audi_evento -> "A"
            )
            .executeInsert()
        }
      }

      result
    }

  }

  /**
    Eliminar Registro
    @param id: Long
    @param usua_id: Long
   */
   def borrar(id: Long, usua_id: Long): Boolean = {
      db.withConnection { implicit connection => 
      val fecha: LocalDate = new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDateTime = new LocalDateTime(Calendar.getInstance().getTimeInMillis())        


      val count: Long =
        SQL("UPDATE siap.ucap_control SET ucco_estado = 9 WHERE ucco_id = {id}").
        on(
            'id -> id
        ).executeUpdate()

      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "ucap_control",
          'audi_uid -> id,
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