import request from '@/utils/request'

export function informe_siap_calculo_carga_xls (periodo, anho, empr_id) {
  const data = {
    periodo,
    anho,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/scc/' + data.periodo + '/' + data.anho + '/' + data.empr_id
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_visita_por_barrio_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/svpb/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_resumen_material_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/srmx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_material_repetido_xls (fecha_inicial, fecha_final, tiel) {
  const data = {
    fecha_inicial,
    fecha_final,
    tiel
  }
  return request({
    url: '/info/smrx/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.tiel,
    method: 'get'
  })
}

export function informe_siap_resumen_material_reporte_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/srmrx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_material_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdmx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_expansion_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdex/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_reubicacion_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdrx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_cambio_medida_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdcmx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_modernizacion_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdzx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_actualizacion_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdax/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_reposicion_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdpx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_repotenciacion_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdwx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_retiro_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdtx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_detallado_retiro_reubicacion_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sdrr/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_inventario (fecha_corte, page_size, current_page) {
  const data = {
    fecha_corte,
    page_size,
    current_page
  }
  return request({
    url: '/info/sig/' + data.fecha_corte + '/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function informe_siap_inventario_xls (fecha_corte, empr_id) {
  const data = {
    fecha_corte,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/sigx/' + data.fecha_corte + '/' + data.empr_id
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_control_xls (fecha_corte, empr_id) {
  const data = {
    fecha_corte,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/scgx/' + data.fecha_corte + '/' + data.empr_id
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_inventario_filtro_xls (fecha_corte, orderby, filter, empr_id) {
  const data = {
    fecha_corte,
    orderby,
    filter,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/sifx/' + data.fecha_corte + '/' + data.orderby + '/' + data.filter + '/' + data.empr_id
  window.open(encodeURI(url), '_self', 'location=no, menubar=no')
}

export function informe_siap_control_filtro_xls (fecha_corte, orderby, filter, empr_id) {
  const data = {
    fecha_corte,
    orderby,
    filter,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/scfx/' + data.fecha_corte + '/' + data.orderby + '/' + data.filter + '/' + data.empr_id
  window.open(encodeURI(url), '_self', 'location=no, menubar=no')
}

export function informe_siap_resumen_material (fecha_inicial, fecha_final, usua_id, empr_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    usua_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/srm/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.usua_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function informe_siap_reporte_por_uso_xls (fecha_toma, usua_id, empr_id) {
  const data = {
    fecha_toma,
    usua_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/srpu/' + data.fecha_toma + '/' + data.usua_id + '/' + data.empr_id
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_resumen_material_reporte (fecha_inicial, fecha_final, usua_id, empr_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    usua_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/srmr/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.usua_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function informe_siap_detallado_material (fecha_inicial, fecha_final, usua_id, empr_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    usua_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + 's/api' + '/info/sdm/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.usua_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function informe_siap_disponibilidad_xls (fecha_inicial, fecha_final, wt, empr_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    wt,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/sdx/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.wt + '/' + data.empr_id
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_eficiencia_xls (fecha_inicial, fecha_final, empr_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/sde/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.empr_id
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_ucap_xls (fecha_inicial, fecha_final, empr_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/sucx/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.empr_id
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_graficos_reporte (fecha_inicial, fecha_final, usua_id, empr_id, num_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    usua_id,
    empr_id,
    num_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/sgr/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.usua_id + '/' + data.empr_id + '/' + data.num_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function informe_siap_reporte_consolidado_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/srcx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}
// window.location.protocol + '//' + window.location.host.split('/')[0] + '/api'
export function informe_siap_medidor_xls (empr_id) {
  const data = {
    empr_id
  }
  const token = '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/smx/' + data.empr_id + '/' + token
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_transformador_xls (empr_id) {
  const data = {
    empr_id
  }
  const token = '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/stx/' + data.empr_id + '/' + token
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_poste_xls (empr_id) {
  const data = {
    empr_id
  }
  const token = '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/spx/' + data.empr_id + '/' + token
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_redes_xls (empr_id) {
  const data = {
    empr_id
  }
  const token = '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/srx/' + data.empr_id + '/' + token
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_solicitud_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sisx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_luminaria_por_reporte_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/slprx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_por_cuadrilla_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/sipcx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function informe_siap_obra_cuadrilla_xls (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/info/siocx/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}
export function informe_siap_cambio_direccion_xls (fecha_inicial, fecha_final, formato) {
  const data = {
    fecha_inicial,
    fecha_final,
    formato
  }
  const token = '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/sicdx/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.formato + '/' + token
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_solicitud_x_vencer_xls () {
  return request({
    url: '/info/sisvx',
    method: 'get'
  })
}

export function informe_siap_muot_xls (fecha_inicial, fecha_final, empr_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    empr_id
  }
  const token = '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/simox/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.empr_id + '/' + token
  window.open(url, '_self', 'location=no, menubar=no')
}

export function informe_siap_general_operaciones_xls (fecha_inicial, fecha_final, formato, empr_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    formato,
    empr_id
  }
  const token = '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/info/sigox/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.formato + '/' + data.empr_id + '/' + token
  window.open(url, '_self', 'location=no, menubar=no')
}
