package models

import javax.inject.Inject
import java.util.Calendar

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

import play.api.db.DBApi

import anorm._
import anorm.SqlParser.{ get, str }
import anorm.JodaParameterMetaData._

import scala.util.{ Failure, Success }
import scala.concurrent.{ Await, Future }

import org.joda.time.DateTime
import org.joda.time.LocalDate

case class Municipio(muni_id:Long, muni_descripcion: String, muni_codigo: Long, depa_id: Long)

object Municipio {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val municipioWrites = new Writes[Municipio] {
        def writes(municipio: Municipio) = Json.obj(
            "muni_id" -> municipio.muni_id,
            "muni_descripcion" -> municipio.muni_descripcion,
            "muni_codigo" -> municipio.muni_codigo,
            "depa_id" -> municipio.depa_id
        )
    }

    implicit val municipioReads: Reads[Municipio] = (
        (__ \ "muni_id").read[Long] and
          (__ \ "muni_descripcion").read[String] and
          (__ \ "muni_codigo").read[Long] and
          (__ \ "depa_id").read[Long]
    )(Municipio.apply _)
}

class MunicipioRepository @Inject()(dbapi: DBApi)(implicit ec:DatabaseExecutionContext){
   private val db = dbapi.database("default")

    /**
    *    Parsear un Municipio desde un ResultSet
    */
    private val simple = {
        get[Long]("municipio.muni_id") ~
          get[String]("municipio.muni_descripcion") ~
          get[Long]("municipio.muni_codigo") ~
          get[Long]("municipio.depa_id") map {
              case muni_id ~ muni_descripcion ~ muni_codigo ~ depa_id => Municipio(muni_id, muni_descripcion, muni_codigo, depa_id)
          }
    }

    /**
    * Recuperar un Municipio por su muni_id
    * @param muni_id: Long
    */
    def buscarPorId(muni_id: Long) : Option[Municipio] = {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.municipio WHERE muni_id = {muni_id}").
            on(
                'muni_id -> muni_id
            ).
            as(simple.singleOpt)
        }
    }

    /**
    *  Recuperar un Municipio por su muni_descripcion
    */
    def buscarPorDescripcion(muni_descripcion: String) : Future[Iterable[Municipio]] = Future[Iterable[Municipio]] {
        db.withConnection { implicit connection =>
            SQL("SELECT * FROM siap.municipio WHERE muni_descripcion LIKE %{muni_descripcion}% ORDER BY muni_descripcion").
            on(
                'muni_descripcion -> muni_descripcion
            ).as(simple *)
        }
    }

    /**
    *  Recupear todos los Municipio de un Departamento usando el depa_id
    *  @param depa_id: Long
    */
    def buscarPorDepartamento(depa_id: Long):Future[Iterable[Municipio]] = Future[Iterable[Municipio]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.municipio WHERE depa_id = {depa_id} ORDER BY muni_descripcion").
            on(
                'depa_id -> depa_id
            ).
            as(simple *)            
        }
    }
    /**
    * Recuperar todos los Municipio
    */
    def todos():Future[Iterable[Municipio]] = Future[Iterable[Municipio]] {
        db.withConnection { implicit connection => 
            SQL("SELECT * FROM siap.municipio ORDER BY muni_descripcion").
            as(simple *)            
        }
    }

}   
