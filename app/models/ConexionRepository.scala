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
import org.joda.time.LocalDateTime

case class Conexion(cone_id: Option[Long], cone_descripcion: String, cone_estado: Int, usua_id: Long)

object Conexion {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val conexionWrite = new Writes[Conexion] {
        def writes(conexion: Conexion) = Json.obj(
            "cone_id" -> conexion.cone_id,
            "cone_descripcion" -> conexion.cone_descripcion,
            "cone_estado" -> conexion.cone_estado,
            "usua_id" -> conexion.usua_id
        )
    }

    implicit val usoRead: Reads[Conexion] = (
        (__ \ "cone_id").readNullable[Long] and
        (__ \ "cone_descripcion").read[String] and
        (__ \ "cone_estado").read[Int] and
        (__ \ "usua_id").read[Long]
    )(Conexion.apply _)
}

class ConexionRepository @Inject()(dbapi: DBApi)(implicit ec: DatabaseExecutionContext) {
    private val db = dbapi.database("default")

    private val simple = {
        get[Option[Long]]("conexion.cone_id") ~
        get[String]("conexion.cone_descripcion") ~
        get[Int]("conexion.cone_estado") ~
        get[Long]("conexion.usua_id") map {
            case cone_id ~ cone_descripcion ~ cone_estado ~ usua_id => Conexion(cone_id, cone_descripcion, cone_estado, usua_id)
        }
    }

    /**
    *  Recuperar todas la Aap de la empresa
    */
    def conexiones(): Future[Iterable[Conexion]] = Future[Iterable[Conexion]] {
                db.withConnection { implicit connection => 
                    SQL("SELECT * FROM siap.conexion WHERE cone_estado <> 9 ORDER BY cone_descripcion ASC").as(simple *)
                }   
    }    
}