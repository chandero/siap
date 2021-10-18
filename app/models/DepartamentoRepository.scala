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

case class Departamento(depa_id: Long, depa_descripcion: String, depa_codigo: Int)

object Departamento {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val depaWrites = new Writes[Departamento] {
      def writes(depa: Departamento) = Json.obj(
          "depa_id" -> depa.depa_id,
          "depa_descripcion" -> depa.depa_descripcion,
          "depa_codigo" -> depa.depa_codigo
      )
  }

  implicit val depaReads: Reads[Departamento] = (
      (__ \ "depa_id").read[Long] and
      (__ \ "depa_descripcion").read[String] and
      (__ \ "depa_codigo").read[Int]
  )(Departamento.apply _)

}

class DepartamentoRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {
    private val db = dbapi.database("default")

    /**
    * Parsear un Departamento desde el ResultSet
    */
    private val simple = {
        get[Long]("departamento.depa_id") ~
        get[String]("departamento.depa_descripcion") ~
        get[Int]("departamento.depa_codigo") map {
            case depa_id ~ depa_descripcion ~ depa_codigo => Departamento(depa_id, depa_descripcion, depa_codigo)
        }
    }

    /*
    * Recuperar un Departamento usando su depa_id
    * @param depa_id: Long
    */
    def buscarPorId(depa_id: Long): Option[Departamento] = {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.departamento WHERE depa_id = {depa_id}").on(
                'depa_id -> depa_id
            ).as(simple.singleOpt)
        }
    }

    /**
        Recuperar todos los departamentos
    */
    def todos(): Future[Iterable[Departamento]] = Future[Iterable[Departamento]] {
        db.withConnection { implicit connection => 
            SQL("SELECT d.* FROM siap.departamento d ORDER BY depa_descripcion")
            .as(simple *)
        }
    }
}