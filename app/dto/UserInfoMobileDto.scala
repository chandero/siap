package dto

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

case class UserInfoMobileDto(usua_id: Long, usua_email: String, usua_nombre: String, usua_apellido: String, empr_id: Long, empr_descripcion: String, token: String, cuad_id: Long, perfil: List[String])

object UserInfoMobileDto {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")

    implicit val userinfodtoWrites = new Writes[UserInfoMobileDto] {
        def writes(userinfo: UserInfoMobileDto) = Json.obj(
            "usua_id" -> userinfo.usua_id,
            "usua_email" -> userinfo.usua_email,
            "usua_nombre" -> userinfo.usua_nombre,
            "usua_apellido" -> userinfo.usua_apellido,
            "empr_id" -> userinfo.empr_id,
            "empr_descripcion" -> userinfo.empr_descripcion,
            "token" -> userinfo.token,
            "cuad_id" -> userinfo.cuad_id,
            "perfil" -> userinfo.perfil
        )
    }

    implicit val userinfodtoReads: Reads[UserInfoMobileDto] = (
        (__ \ "usua_id").read[Long] and
        (__ \ "usua_email").read[String] and
        (__ \ "usua_nombre").read[String] and
        (__ \ "usua_apellido").read[String] and        
        (__ \ "empr_id").read[Long] and
        (__ \ "empr_descripcion").read[String] and
        (__ \ "token").read[String] and
        (__ \ "cuad_id").read[Long] and
        (__ \ "perfil").read[List[String]]
    )(UserInfoMobileDto.apply _)
}