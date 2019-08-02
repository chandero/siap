package dto

import play.api.libs.json._
import play.api.libs.json.JodaReads
import play.api.libs.json.JodaWrites
import play.api.libs.functional.syntax._

case class UsuarioDto(usua_id: Option[Long], usua_email: String, usua_clave: Option[String], usua_nombre: String, usua_apellido: String, token: Option[String], perf_id: Long)

object UsuarioDto {
    implicit val yourJodaDateReads = JodaReads.jodaDateReads("yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    implicit val yourJodaDateWrites = JodaWrites.jodaDateWrites("yyyy-MM-dd'T'HH:mm:ss.SSSZ'")
    
    implicit val usuariodtoWrites = new Writes[UsuarioDto] {
      def writes(usuario: UsuarioDto) = Json.obj(
        "usua_id" -> usuario.usua_id,
        "usua_email" -> usuario.usua_email,
        "usua_clave" -> usuario.usua_clave,
        "usua_nombre" -> usuario.usua_nombre,
        "usua_apellido" -> usuario.usua_apellido,
        "token" -> usuario.token,
        "perf_id" -> usuario.perf_id
      )
    }

    implicit val usuariodtoReads: Reads[UsuarioDto] = (
        (__ \ "usua_id").readNullable[Long] and
          (__ \ "usua_email").read[String] and
          (__ \ "usua_clave").readNullable[String] and
          (__ \ "usua_nombre").read[String] and
          (__ \ "usua_apellido").read[String] and
          (__ \ "token").readNullable[String] and
          (__ \ "perf_id").read[Long]
    )(UsuarioDto.apply _)   
}