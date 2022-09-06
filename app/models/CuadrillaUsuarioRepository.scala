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

case class CuadrillaUsuario(cuad_id: Option[Long], usua_id: Option[Long], cuus_esreponsable: Option[Boolean])

object CuadrillaUsuario {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val cuadrillausuarioWrites = new Writes[CuadrillaUsuario] {
    def writes(cuadrillausuario: CuadrillaUsuario) = Json.obj(
      "cuad_id" -> cuadrillausuario.cuad_id,
      "usua_id" -> cuadrillausuario.usua_id,
      "cuus_esresponsable" -> cuadrillausuario.cuus_esreponsable
    )
  }

  implicit val cuadrillausuarioReads: Reads[CuadrillaUsuario] = (
    (__ \ "cuad_id").readNullable[Long] and
      (__ \ "usua_id").readNullable[Long] and
      (__ \ "cuus_esresponsable").readNullable[Boolean]
  )(CuadrillaUsuario.apply _)

    /**
    Parsear un CuadrillaUsuario desde un ResultSet
    */
  val _set = {
    get[Option[Long]]("cuad_id") ~
      get[Option[Long]]("usua_id") ~ 
      get[Option[Boolean]]("cuus_esresponsable") map {
      case cuad_id ~ usua_id ~ cuus_esreponsable => CuadrillaUsuario(cuad_id, usua_id, cuus_esreponsable)
    }
  }
}

class CuadrillaUsuarioRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")



  /**
    Recuperar lista de usuarios perteneciente a una Cuadrilla
    */
  def buscarPorId(cuad_id: Long): Future[Iterable[CuadrillaUsuario]] =
    Future[Iterable[CuadrillaUsuario]] {
      db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.cuadrilla_usuario WHERE cuad_id = {cuad_id}")
          .on(
            'cuad_id -> cuad_id
          )
          .as(CuadrillaUsuario._set *)
      }
    }

  /**
    Crear usuarios en cuadrilla recibiendo la lista
    @param cuad_id
    @param List[Int] usua_id
    */
  def crear(cuad_id: Long, usuarios: List[Int]): Future[Boolean] = Future {
    if (eliminar(cuad_id) == true) {
      db.withConnection { implicit connection =>
        val fecha: LocalDate =
          new LocalDate(Calendar.getInstance().getTimeInMillis())
        val hora: LocalDate = fecha
        var count: Int = 0
        for (usua_id <- usuarios) {
          val result: Boolean = SQL(
            "INSERT INTO siap.cuadrillausuario (cuad_id, usua_id) VALUES ({cuad_id},{usua_id}")
            .on(
              'cuad_id -> cuad_id,
              'usua_id -> usua_id
            )
            .execute
          if (result) {
            count += 1
          }
        }
        count == usuarios.length
      }
    } else { false }
  }

  /**
    Eliminar usuarios de una cuadrilla
    */
  def eliminar(cuad_id: Long): Boolean = {
    db.withConnection { implicit connection =>
      SQL("DELETE FROM siap.cuadrilla_usuario WHERE cuad_id = {cuad_id}")
        .on(
          'cuad_id -> cuad_id
        )
        .executeUpdate() > 0
    }
  }

  def getUsuariosCuadrilla(empr_id: Long) = Future[Iterable[CuadrillaUsuario]] {
    db.withConnection {
      implicit connection =>
        val result = SQL(
          """
          SELECT cu1.cuad_id, cu1.usua_id, cu1.cuus_esresponsable FROM siap.usuario u1
          LEFT JOIN siap.cuadrilla_usuario cu1 ON cu1.usua_id = u1.usua_id
          LEFT JOIN siap.usuario_empresa_perfil uep1 ON uep1.usua_id = u1.usua_id and uep1.empr_id = {empr_id}
          WHERE uep1.empr_id = {empr_id} and uep1.perf_id = {perf_id}
          """
        ).on(
            'empr_id -> empr_id,
            'perf_id -> 6
          )
          .as(CuadrillaUsuario._set *)
        result
    }
  }

  def actualizarUsuarioCuadrilla(empr_id: Long, cuad_id: Long, usua_id: Long, cuus_esreponsable: Boolean): Boolean = {
    db.withConnection { implicit connection =>
      val actualizado = SQL("UPDATE siap.cuadrilla_usuario SET cuad_id = {cuad_id}, cuus_esresponsable = {cuus_esreponsable} WHERE usua_id = {usua_id} ")
        .on(
          'cuad_id -> cuad_id,
          'usua_id -> usua_id,
          'cuus_esreponsable -> cuus_esreponsable
        )
        .executeUpdate() > 0

      if (!actualizado) {
        val creado = SQL("INSERT INTO siap.cuadrilla_usuario (cuad_id, usua_id, cuus_esreponsable) VALUES ({cuad_id}, {usua_id}, {cuus_esreponsable})")
          .on(
            'cuad_id -> cuad_id,
            'usua_id -> usua_id,
            'cuus_esreponsable -> cuus_esreponsable
          )
          .executeUpdate() > 0
        creado
      } else {
        actualizado
      }
    }
  }

}
