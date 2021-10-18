package dto

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

case class ReporteWebDto(
    acti_id: Option[Long],
    barr_id: Option[Long],
    empr_id: Option[Long],
    repo_captcha: Option[Long],
    repo_descripcion: Option[String],
    repo_direccion: Option[String],
    repo_email: Option[String],
    repo_fecharecepcion: Option[String],
    repo_nombre: Option[String],
    repo_telefono: Option[String],
    token: Option[String]
)

object ReporteWebDto {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val wWrites = new Writes[ReporteWebDto] {
        def writes(e: ReporteWebDto) = Json.obj(
            "acti_id" -> e.acti_id,
            "barr_id" -> e.barr_id,
            "empr_id" -> e.empr_id,
            "repo_captcha" -> e.repo_captcha,
            "repo_descripcion" -> e.repo_descripcion,
            "repo_direccion" -> e.repo_direccion,
            "repo_email" -> e.repo_email,
            "repo_fecharecepcion" -> e.repo_fecharecepcion,
            "repo_nombre" -> e.repo_nombre,
            "repo_telefono" -> e.repo_telefono,
            "token" -> e.token
        )
    }

    implicit val rReads: Reads[ReporteWebDto] = (
        (__ \ "acti_id").readNullable[Long] and
        (__ \ "barr_id").readNullable[Long] and
        (__ \ "empr_id").readNullable[Long] and
        (__ \ "repo_captcha").readNullable[Long] and        
        (__ \ "repo_descripcion").readNullable[String] and
        (__ \ "repo_direccion").readNullable[String] and
        (__ \ "repo_email").readNullable[String] and
        (__ \ "repo_fecharecepcion").readNullable[String] and
        (__ \ "repo_nombre").readNullable[String] and
        (__ \ "repo_telefono").readNullable[String] and
        (__ \ "token").readNullable[String]
    )(ReporteWebDto.apply _)
}