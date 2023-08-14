package dto

case class MedidorAapDto(
    aap_id: Option[Long],
    aap_descripcion: Option[String],
    aap_apoyo: Option[String],
    aama_id: Option[Long],
    aamo_id: Option[Long],
    aacu_id: Option[Long],
    empr_id: Option[Long],
    usua_id: Option[Long],
    aap_direccion: Option[String],
    barr_id: Option[Long],
    esta_id: Option[Int],
    aap_acta: Option[String],    
  )