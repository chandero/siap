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

case class CuadrillaUsuario(cuad_id: Long, usua_id: Long)

object CuadrillaUsuario {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val cuadrillausuarioWrites = new Writes[CuadrillaUsuario] {
    def writes(cuadrillausuario: CuadrillaUsuario) = Json.obj(
      "cuad_id" -> cuadrillausuario.cuad_id,
      "usua_id" -> cuadrillausuario.usua_id
    )
  }

  implicit val cuadrillausuarioReads: Reads[CuadrillaUsuario] = (
    (__ \ "cuad_id").read[Long] and
      (__ \ "usua_id").read[Long]
  )(CuadrillaUsuario.apply _)
}

class CuadrillaUsuarioRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
    Parsear un CuadrillaUsuario desde un ResultSet
    */
  private val simple = {
    get[Long]("cuadrillausuario.cuad_id") ~
      get[Long]("cuadrillausuario.usua_id") map {
      case cuad_id ~ usua_id => CuadrillaUsuario(cuad_id, usua_id)
    }
  }

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
          .as(simple *)
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

}
