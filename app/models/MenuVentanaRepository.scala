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

case class MenuVentana(meve_id: Long,
                       meve_titulo: String,
                       meve_componente: String,
                       meve_posicion: Int,
                       megr_id: Long)

object MenuVentana {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val menuventanaWrites = new Writes[MenuVentana] {
    def writes(menuventana: MenuVentana) = Json.obj(
      "meve_id" -> menuventana.meve_id,
      "meve_titulo" -> menuventana.meve_titulo,
      "meve_componente" -> menuventana.meve_componente,
      "meve_posicion" -> menuventana.meve_posicion,
      "megr_id" -> menuventana.megr_id
    )
  }

  implicit val menuventanaReads: Reads[MenuVentana] = (
    (__ \ "meve_id").read[Long] and
      (__ \ "meve_titulo").read[String] and
      (__ \ "meve_componente").read[String] and
      (__ \ "meve_posicion").read[Int] and
      (__ \ "megr_id").read[Long]
  )(MenuVentana.apply _)
}

class MenuVentanaRepository @Inject()(dbapi: DBApi)(
    implicit ec: DatabaseExecutionContext) {
  private val db = dbapi.database("default")

  /**
            Parsear un MenuVentana desde un ResultSet
    */
  private val simple = {
    get[Long]("menuventana.meve_id") ~
      get[String]("menuventana.meve_titulo") ~
      get[String]("menuventana.meve_componente") ~
      get[Int]("menuventana.meve_posicion") ~
      get[Long]("menuventana.megr_id") map {
      case meve_id ~ meve_titulo ~ meve_componente ~ meve_posicion ~ megr_id =>
        MenuVentana(meve_id,
                    meve_titulo,
                    meve_componente,
                    meve_posicion,
                    megr_id)
    }
  }

  /**
    Recuperar todos los MenuVentana
   */
   def todos(): Future[Iterable[MenuVentana]] = Future[Iterable[MenuVentana]] {
       db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.menuventana").as(simple *)
       }
   }

   /**
     Recuperar MenuVentana por megr_id
     @param megr_id: Long
    */
    def buscarPorMenuGrupo(megr_id: Long): Future[Iterable[MenuVentana]] = Future[Iterable[MenuVentana]] {
       db.withConnection { implicit connection =>
        SQL("SELECT * FROM siap.menuventana WHERE megr_id = {megr_id}").
        on(
          'megr_id -> megr_id
        ).as(simple *)
       }
    }
}
