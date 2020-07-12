package dto

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

case class CuadrillaDto(cuad_id: Option[Long], cuad_descripcion: String, cuad_estado: Int, usua_id: Long, usuarios: List[Long])

object CuadrillaDto {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val cuadrilladtoWrites = new Writes[CuadrillaDto] {
        def writes(cuadrilla: CuadrillaDto) = Json.obj(
            "cuad_id" -> cuadrilla.cuad_id,
            "cuad_descripcion" -> cuadrilla.cuad_descripcion,
            "cuad_estado" -> cuadrilla.cuad_estado,
            "usua_id" -> cuadrilla.usua_id,
            "usuarios" -> cuadrilla.usuarios
        )
    }

    implicit val cuadrilladtoReads: Reads[CuadrillaDto] = (
        (__ \ "cuad_id").readNullable[Long] and
        (__ \ "cuad_descripcion").read[String] and
        (__ \ "cuad_estado").read[Int] and
        (__ \ "usua_id").read[Long] and
        (__ \ "usuarios").read[List[Long]]
    )(CuadrillaDto.apply _)
}