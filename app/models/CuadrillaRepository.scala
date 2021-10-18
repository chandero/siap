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

import com.jaroop.anorm.relational._

import Usuario._

case class Cuadrilla(cuad_id: Option[Long],
                     cuad_descripcion: String,
                     cuad_estado: Int,
                     usua_id: Long,
                     empr_id: Long,
                     usuarios: List[Usuario] = Nil)

object Cuadrilla {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val cuadrillaWrites = new Writes[Cuadrilla] {
    def writes(cuadrilla: Cuadrilla) = Json.obj(
      "cuad_id" -> cuadrilla.cuad_id,
      "cuad_descripcion" -> cuadrilla.cuad_descripcion,
      "cuad_estado" -> cuadrilla.cuad_estado,
      "usua_id" -> cuadrilla.usua_id,
      "empr_id" -> cuadrilla.empr_id,
      "usuarios" -> cuadrilla.usuarios
    )
  }

  implicit val cuadrillaReads: Reads[Cuadrilla] = (
    (__ \ "cuad_id").readNullable[Long] and
      (__ \ "cuad_descripcion").read[String] and
      (__ \ "cuad_estado").read[Int] and
      (__ \ "usua_id").read[Long] and
      (__ \ "empr_id").read[Long] and
      (__ \ "usuarios").read[List[Usuario]]
  )(Cuadrilla.apply _)

}

class CuadrillaRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
        Parsear un Cuadrilla desde un ResultSet
    */
  private val simple = {
    get[Option[Long]]("cuadrilla.cuad_id") ~
      get[String]("cuadrilla.cuad_descripcion") ~
      get[Int]("cuadrilla.cuad_estado") ~
      get[Long]("cuadrilla.usua_id") ~
      get[Long]("cuadrilla.empr_id") map {
      case cuad_id ~ cuad_descripcion ~ cuad_estado ~ usua_id ~ empr_id  =>
        Cuadrilla(cuad_id, cuad_descripcion, cuad_estado, usua_id, empr_id)
    }
  }

  private val parser: RowParser[(Cuadrilla, Option[Usuario])] = {
     simple ~ Usuario.simple.? map {
       case cuadrilla~usuario => (cuadrilla, usuario)
     }
  }

  private val relationalParser = RelationalParser(simple, Usuario.simple)

  implicit val rf = RowFlattener[Cuadrilla, Usuario] { (cuadrilla, usuarios) => cuadrilla.copy(usuarios = usuarios)}

  /**
        Recuperar un cuadrilla por su cuad_id
    */
  def buscarPorId(cuad_id: Long): Option[Cuadrilla] = {
    db.withConnection { implicit connection =>
      SQL("SELECT * FROM siap.cuadrilla c LEFT OUTER JOIN siap.cuadrilla_usuario cu ON c.cuad_id = cu.cuad_id LEFT OUTER JOIN siap.usuario u ON cu.usua_id = u.usua_id WHERE c.cuad_id={cuad_id}")
        .on(
          'cuad_id -> cuad_id
        )
        .asRelational(relationalParser.singleOpt)
    }
  }

  /**
        Recuperar cuadrilla por su cuad_descripcion
    */
  def buscarPorDescripcion(cuad_descripcion: String,
                           empr_id: Long): Future[Iterable[Cuadrilla]] =
    Future[Iterable[Cuadrilla]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.cuadrilla WHERE cuad_descripcion LIKE %{cuad_descripcion}% ORDER BY cuad_descripcion")
          .on(
            'cuad_descripcion -> cuad_descripcion
          )
          .as(simple *)
      }
    }

  /**
  * Recuperar total de registros
  * @return total
  */
  def cuenta(empr_id: Long): Long =  {
    db.withConnection{ implicit connection =>
      val result = SQL("SELECT COUNT(*) AS c FROM siap.cuadrilla WHERE empr_id = {empr_id} and cuad_estado <> 9").
      on(
        'empr_id -> empr_id
      ).as(SqlParser.scalar[Long].single)
      result
    }
  }

  /**
    * Recuperar todas la Cuadrilla de una empresa páginado
    * @param empr_id: Long
    * @param page_size: Long
    * @param current_page: Long        
    */
  def todos(empr_id: Long, page_size: Long, current_page:Long): Future[Iterable[Cuadrilla]] =
    Future[Iterable[Cuadrilla]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.cuadrilla WHERE empr_id = {empr_id} and cuad_estado <> 9 ORDER BY cuad_descripcion LIMIT {page_size} OFFSET {page_size} * ({current_page} - 1) ")
          .on(
            'empr_id -> empr_id,
            'page_size -> page_size,
            'current_page -> current_page
          )
          .as(simple *)
      }
    }

  /**
        Recuperar todas la Cuadrilla de una empresa
    */
  def cuadrillas(empr_id: Long): Future[Iterable[Cuadrilla]] =
    Future[Iterable[Cuadrilla]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.cuadrilla WHERE empr_id = {empr_id} ORDER BY cuad_descripcion")
          .on(
            'empr_id -> empr_id
          )
          .as(simple *)
      }
    }

  /**
        Crear cuadrilla
        @param cuadrilla: Cuadrilla
    */
  def crear(cuadrilla: Cuadrilla): Future[Long] = Future {
    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val id: Long = SQL(
        "INSERT INTO siap.cuadrilla (cuad_descripcion, cuad_estado, usua_id, empr_id) VALUES ({cuad_descripcion}, {cuad_estado}, {usua_id}, {empr_id})")
        .on(
          'cuad_descripcion -> cuadrilla.cuad_descripcion,
          'cuad_estado -> cuadrilla.cuad_estado,
          'usua_id -> cuadrilla.usua_id,
          'empr_id -> cuadrilla.empr_id
        )
        .executeInsert()
        .get

      // Guardar Usuarios
      cuadrilla.usuarios.foreach { usuario =>
        val cuus_id = SQL("INSERT INTO siap.cuadrilla_usuario (cuad_id, usua_id) VALUES ({cuad_id}, {usua_id})").
        on(
          'cuad_id -> id,
          'usua_id -> usuario.usua_id
        ).executeInsert().get
      }
      //

      SQL("INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> cuadrilla.usua_id,
          'audi_tabla -> "cuadrilla",
          'audi_uid -> id,
          'audi_campo -> "cuad_descripcion",
          'audi_valorantiguo -> "",
          'audi_valornuevo -> cuadrilla.cuad_descripcion,
          'audi_evento -> "I"
        )
        .executeInsert()

      id
    }
  }

  /**
   Actualizar Cuadrilla
    */
  def actualizar(cuadrilla: Cuadrilla): Boolean = {
    val cuadrilla_ant: Option[Cuadrilla] = buscarPorId(cuadrilla.cuad_id.get)

    db.withConnection { implicit connection =>
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha
      val result: Boolean = SQL(
        "UPDATE siap.cuadrilla SET cuad_descripcion = {cuad_descripcion}, cuad_estado = {cuad_estado}, empr_id = {empr_id}, usua_id = {usua_id} WHERE cuad_id = {cuad_id}")
        .on(
          'cuad_descripcion -> cuadrilla.cuad_descripcion,
          'cuad_estado -> cuadrilla.cuad_estado,
          'empr_id -> cuadrilla.empr_id,
          'usua_id -> cuadrilla.usua_id,
          'cuad_id -> cuadrilla.cuad_id
        )
        .executeUpdate() > 0

      // Guardar Usuarios
      // Borrar usuarios anteriores
      SQL("DELETE FROM siap.cuadrilla_usuario WHERE cuad_id = {cuad_id}").
      on(
        'cuad_id -> cuadrilla.cuad_id
      ).execute()
      // Adicionar los nuevos
      cuadrilla.usuarios.foreach { usuario =>
        val cuus_id = SQL("INSERT INTO siap.cuadrilla_usuario (cuad_id, usua_id) VALUES ({cuad_id}, {usua_id})").
        on(
          'cuad_id -> cuadrilla.cuad_id,
          'usua_id -> usuario.usua_id
        ).executeInsert().get
      }
      //

      if (cuadrilla_ant != None) {
        if (cuadrilla_ant.get.cuad_descripcion != cuadrilla.cuad_descripcion) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> cuadrilla.usua_id,
              'audi_tabla -> "cuadrilla",
              'audi_uid -> cuadrilla.cuad_id,
              'audi_campo -> "cuad_descripcion",
              'audi_valorantiguo -> cuadrilla_ant.get.cuad_descripcion,
              'audi_valornuevo -> cuadrilla.cuad_descripcion,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (cuadrilla_ant.get.cuad_estado != cuadrilla.cuad_estado) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> cuadrilla.usua_id,
              'audi_tabla -> "cuadrilla",
              'audi_uid -> cuadrilla.cuad_id,
              'audi_campo -> "cuad_estado",
              'audi_valorantiguo -> cuadrilla_ant.get.cuad_estado,
              'audi_valornuevo -> cuadrilla.cuad_estado,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (cuadrilla_ant.get.empr_id != cuadrilla.empr_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> cuadrilla.usua_id,
              'audi_tabla -> "cuadrilla",
              'audi_uid -> cuadrilla.cuad_id,
              'audi_campo -> "empr_id",
              'audi_valorantiguo -> cuadrilla_ant.get.empr_id,
              'audi_valornuevo -> cuadrilla.empr_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }

        if (cuadrilla_ant.get.usua_id != cuadrilla.usua_id) {
          SQL(
            "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
            .on(
              'audi_fecha -> fecha,
              'audi_hora -> hora,
              'usua_id -> cuadrilla.usua_id,
              'audi_tabla -> "cuadrilla",
              'audi_uid -> cuadrilla.cuad_id,
              'audi_campo -> "usua_id",
              'audi_valorantiguo -> cuadrilla_ant.get.usua_id,
              'audi_valornuevo -> cuadrilla.usua_id,
              'audi_evento -> "A"
            )
            .executeInsert()
        }         

      }
      result
    }
  }

  /**
    Borrar cuadrilla
   */
   def borrar(empr_id: Long, cuad_id:Long, usua_id: Long): Boolean = {
     db.withConnection { implicit connection => 
      val fecha: LocalDate =
        new LocalDate(Calendar.getInstance().getTimeInMillis())
      val hora: LocalDate = fecha

      val count: Long =
        SQL("UPDATE siap.cuadrilla SET cuad_estado = 9 WHERE cuad_id = {cuad_id}").
        on(
            'cuad_id -> cuad_id
        ).executeUpdate()
      SQL(
        "INSERT INTO siap.auditoria(audi_fecha, audi_hora, usua_id, audi_tabla, audi_uid, audi_campo, audi_valorantiguo, audi_valornuevo, audi_evento) VALUES ({audi_fecha}, {audi_hora}, {usua_id}, {audi_tabla}, {audi_uid}, {audi_campo}, {audi_valorantiguo}, {audi_valornuevo}, {audi_evento})")
        .on(
          'audi_fecha -> fecha,
          'audi_hora -> hora,
          'usua_id -> usua_id,
          'audi_tabla -> "cuadrilla",
          'audi_uid -> cuad_id,
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
