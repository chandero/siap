package dto

case class ReporteAdicionalDto(
    repo_id: Option[scala.Long],
    repo_fechadigitacion: Option[String],
    repo_modificado: Option[String],
    repo_tipo_expansion: Option[Int],
    repo_luminaria: Option[Boolean],
    repo_redes: Option[Boolean],
    repo_poste: Option[Boolean],
    repo_subreporte: Option[Boolean],
    repo_subid: Option[scala.Long],
    repo_email: Option[String],
    acti_id: Option[scala.Long],
    repo_codigo: Option[String],
    repo_apoyo: Option[String],
    urba_id: Option[scala.Long],
    muot_id: Option[scala.Long],
    medi_id: Option[scala.Long],
    tran_id: Option[scala.Long],
    medi_acta: Option[String],
    aaco_id_anterior: Option[scala.Long],
    aaco_id_nuevo: Option[scala.Long],
    ortr_id: Option[Int]
)

case class EventoDto(
    even_fecha: Option[String],
    even_codigo_instalado: Option[String],
    even_cantidad_instalado: Option[Double],
    even_codigo_retirado: Option[String],
    even_cantidad_retirado: Option[Double],
    even_estado: Option[Int],
    aap_id: Option[Long],
    repo_id: Option[Long],
    elem_id: Option[Long],
    usua_id: Option[Long],
    empr_id: Option[Long],
    even_id: Option[Long],
    unit_id: Option[Long]
)

case class ReporteNovedadDto(
    tireuc_id: Option[scala.Long],
    repo_id: Option[scala.Long],
    nove_id: Option[Int],
    even_id: Option[Int],
    even_estado: Option[Int],
    reno_horaini: Option[String],
    reno_horafin: Option[String],
    reno_observacion: Option[String]
)

case class ReporteDireccionDatoDto(
    aatc_id: Option[scala.Long],
    aatc_id_anterior: Option[scala.Long],
    aama_id: Option[scala.Long],
    aama_id_anterior: Option[scala.Long],
    aamo_id: Option[scala.Long],
    aamo_id_anterior: Option[scala.Long],
    aaco_id: Option[scala.Long],
    aaco_id_anterior: Option[scala.Long],
    aap_potencia: Option[Int],
    aap_potencia_anterior: Option[Int],
    aap_tecnologia: Option[String],
    aap_tecnologia_anterior: Option[String],
    aap_brazo: Option[String],
    aap_brazo_anterior: Option[String],
    aap_collarin: Option[String],
    aap_collarin_anterior: Option[String],
    tipo_id: Option[scala.Long],
    tipo_id_anterior: Option[scala.Long],
    aap_poste_altura: Option[Double],
    aap_poste_altura_anterior: Option[Double],
    aap_poste_propietario: Option[String],
    aap_poste_propietario_anterior: Option[String]
)

case class ReporteDireccionDatoAdicionalDto(
    aacu_id_anterior: Option[scala.Long],
    aacu_id: Option[scala.Long],
    aaus_id_anterior: Option[scala.Long],
    aaus_id: Option[scala.Long],
    medi_id_anterior: Option[scala.Long],
    medi_id: Option[scala.Long],
    tran_id_anterior: Option[scala.Long],
    tran_id: Option[scala.Long],
    aap_apoyo_anterior: Option[String],
    aap_apoyo: Option[String],
    aap_lat_anterior: Option[String],
    aap_lat: Option[String],
    aap_lng_anterior: Option[String],
    aap_lng: Option[String]
)

case class ReporteDireccionFotoDto(
  tireuc_id: Option[scala.Long],
  repo_id: Option[scala.Long],
  aap_id: Option[scala.Long],
  refo_id: Option[Int],
  refo_tipo: Option[Int],
  refo_data: Option[String]
)


case class ReporteDireccionDto(
    repo_id: Option[scala.Long],
    aap_id: Option[scala.Long],
    even_direccion: Option[String],
    barr_id: Option[scala.Long],
    even_direccion_anterior: Option[String],
    barr_id_anterior: Option[scala.Long],
    even_id: Option[Int],
    even_estado: Option[Int],
    even_horaini: Option[String],
    even_horafin: Option[String],
    tire_id: Option[scala.Long],
    coau_codigo: Option[String],
    aap_fechatoma: Option[String],
    dato: Option[ReporteDireccionDatoDto],
    dato_adicional: Option[ReporteDireccionDatoAdicionalDto],
    fotos: Option[List[ReporteDireccionFotoDto]]
)

case class ReporteMobileDto(
    repo_id: Option[scala.Long],
    tireuc_id: Option[scala.Long],
    reti_id: Option[scala.Long],
    repo_consecutivo: Option[scala.Long],
    repo_fecharecepcion: Option[String],
    repo_direccion: Option[String],
    repo_nombre: Option[String],
    repo_telefono: Option[String],
    repo_fechasolucion: Option[String],
    repo_horainicio: Option[String],
    repo_horafin: Option[String],
    repo_reportetecnico: Option[String],
    repo_descripcion: Option[String],
    repo_subrepoconsecutivo: Option[String],
    rees_id: Option[scala.Long],
    orig_id: Option[scala.Long],
    barr_id: Option[scala.Long],
    empr_id: Option[scala.Long],
    tiba_id: Option[scala.Long],
    usua_id: Option[scala.Long],
    adicional: Option[ReporteAdicionalDto],
    meams: Option[List[scala.Long]],
    eventos: Option[List[EventoDto]],
    direcciones: Option[List[ReporteDireccionDto]],
    novedades: Option[List[ReporteNovedadDto]]
)

/* object ReporteDireccionDto {
    var toReporteDireccion = {
        ReporteDireccion(
            repo_id: Option[scala.Long],
            aap_id: Option[scala.Long],
            even_direccion: Option[String],
            barr_id: Option[scala.Long],
            even_direccion_anterior: Option[String],
            barr_id_anterior: Option[scala.Long],
            even_id: Option[Int],
            even_estado: Option[Int],
            even_horaini: Option[String],
            even_horafin: Option[String],
            tire_id: Option[scala.Long],
            coau_codigo: Option[String],
            aap_fechatoma: Option[String],
            dato: Option[ReporteDireccionDatoDto],
            dato_adicional: Option[ReporteDireccionDatoAdicionalDto],
            fotos: Option[List[ReporteDireccionFotoDto]]
        )
    }
} */