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

case class MenuGrupo(megr_id:Long, megr_titulo: String, megr_posicion: Int)

object MenuGrupo {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")    

  implicit val menugrupoWrites = new Writes[MenuGrupo] {
      def writes(menugrupo: MenuGrupo) = Json.obj(
          "megr_id" -> menugrupo.megr_id,
          "megr_titulo" -> menugrupo.megr_titulo,
          "megr_posicion" -> menugrupo.megr_posicion
      )
  }

  implicit val menugrupoReads: Reads[MenuGrupo] = (
      (__ \ "megr_id").read[Long] and
         (__ \ "megr_titulo").read[String] and
         (__ \ "megr_posicion").read[Int]
  )(MenuGrupo.apply _)
}

class MenuGrupoRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext){
    private val db = dbapi.database("default")

    /**
        Parsear un MenuGrupo desde un ResultSet
     */
     private val simple = {
         get[Long]("menugrupo.megr_id") ~
         get[String]("menugrupo.megr_titulo") ~
         get[Int]("menugrupo.megr_posicion") map {
             case megr_id ~ megr_titulo ~ megr_posicion => MenuGrupo(megr_id, megr_titulo, megr_posicion)
         }
     }

     /**
        Recuperar todos los menugrupo
      */
      def todos(): Future[Iterable[MenuGrupo]] = Future[Iterable[MenuGrupo]] {
          db.withConnection { implicit connection =>
           SQL("SELECT * FROM siap.menugrupo").as(simple *)
          }
      }
}