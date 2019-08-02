package dto

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

case class EmpresaDto(empr_id: Option[Long], empr_descripcion: String, token: String, perfil: String, muni_descripcion: String, depa_descripcion: String, empr_sigla: Option[String])

object EmpresaDto {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val empresadtoWrites = new Writes[EmpresaDto] {
        def writes(empresa: EmpresaDto) = Json.obj(
            "empr_id" -> empresa.empr_id,
            "empr_descripcion" -> empresa.empr_descripcion,
            "token" -> empresa.token,
            "perfil" -> empresa.perfil,
            "muni_descripcion" -> empresa.muni_descripcion,
            "depa_descripcion" -> empresa.depa_descripcion,
            "empr_sigla" -> empresa.empr_sigla
        )
    }

    implicit val empresadtoReads: Reads[EmpresaDto] = (
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "empr_descripcion").read[String] and
        (__ \ "token").read[String] and
        (__ \ "perfil").read[String] and
        (__ \ "muni_descripcion").read[String] and
        (__ \ "depa_descripcion").read[String] and
        (__ \ "empr_sigla").readNullable[String]
    )(EmpresaDto.apply _)
}