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

case class ElementoCaracteristica(elca_id: Option[Long],
                                  elca_valor: String,
                                  cara_id: Long,
                                  elem_id: Long,
                                  elca_estado: Int)

object ElementoCaracteristica {
  implicit val yourJodaDateReads =
    JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
  implicit val yourJodaDateWrites =
    JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

  implicit val elementocaracteristicaWrites =
    new Writes[ElementoCaracteristica] {
      def writes(elemento_caracteristica: ElementoCaracteristica) = Json.obj(
        "elca_id" -> elemento_caracteristica.elca_id,
        "elca_valor" -> elemento_caracteristica.elca_valor,
        "cara_id" -> elemento_caracteristica.cara_id,
        "elem_id" -> elemento_caracteristica.elem_id,
        "elca_estado" -> elemento_caracteristica.elca_estado
      )
    }

  implicit val elementocaracteristicaReads: Reads[ElementoCaracteristica] = (
    (__ \ "elca_id").readNullable[Long] and
      (__ \ "elca_valor").read[String] and
      (__ \ "cara_id").read[Long] and
      (__ \ "elem_id").read[Long] and
      (__ \ "elca_estado").read[Int]
  )(ElementoCaracteristica.apply _)

  /**
    Parsear un ElementoCaracteristica desde un ResultSet
    */
  private val elemento_caracteristica = {
    get[Option[Long]]("elemento_caracteristica.elca_id") ~
      get[String]("elemento_caracteristica.elca_valor") ~
      get[Long]("elemento_caracteristica.cara_id") ~
      get[Long]("elemento_caracteristica.elem_id") ~
      get[Int]("elemento_caracteristica.elca_estado") map {
      case elca_id ~ elca_valor ~ cara_id ~ elem_id ~ elca_estado =>
        ElementoCaracteristica(elca_id, elca_valor, cara_id, elem_id, elca_estado)
    }
  }  
}
