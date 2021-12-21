package models

import org.joda.time.DateTime

import anorm._
import anorm.SqlParser.{get, str, int, double, date, bool, scalar, flatten}
import anorm.JodaParameterMetaData._

case class cobro_factura (
    cofa_id: Option[scala.Long],
    cofa_fecha: Option[DateTime],
    cofa_anho: Option[Int],
    cofa_periodo: Option[Int],
    cofa_factura: Option[scala.Long],
    cofa_prefijo: Option[String],
    ordenes: List[orden_trabajo_cobro]
)

object cobro_fatura {
    val _set = {
        get[Option[scala.Long]]("cofa_id") ~
        get[Option[DateTime]]("cofa_fecha") ~
        get[Option[Int]]("cofa_anho") ~
        get[Option[Int]]("cofa_periodo") ~
        get[Option[scala.Long]]("cofa_factura") ~
        get[Option[String]]("cofa_prefijo") map {
            case cofa_id ~ cofa_fecha ~ cofa_anho ~ cofa_periodo ~ cofa_factura ~ cofa_prefijo => cobro_factura(
                cofa_id,
                cofa_fecha,
                cofa_anho,
                cofa_periodo,
                cofa_factura,
                cofa_prefijo,
                Nil
            )
        }
    }
}