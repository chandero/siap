import request from '@/utils/request'

export function getTodos(page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/repo/get',
    method: 'post',
    data: data
  })
}

export function getReportes() {
  return request({
    url: '/repo/all',
    method: 'get'
  })
}

export function getReportesPorAap(aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/repo/gbaap/' + data.aap_id,
    method: 'get'
  })
}

export function siap_reporte_vencido() {
  return request({
    url: '/repo/venc/get',
    method: 'get'
  })
}

export function getReportesRango(anho, mes, tireuc_id) {
  const data = {
    anho,
    mes,
    tireuc_id
  }
  return request({
    url: '/repo/rang/' + data.anho + '/' + data.mes + '/' + data.tireuc_id,
    method: 'get'
  })
}

export function getReportesMigracionRango(anho, mes, tireuc_id) {
  const data = {
    anho,
    mes,
    tireuc_id
  }
  return request({
    url: '/repo/rangm/' + data.anho + '/' + data.mes + '/' + data.tireuc_id,
    method: 'get'
  })
}

export function getReporte(repo_id) {
  const data = {
    repo_id
  }
  return request({
    url: '/repo/' + data.repo_id,
    method: 'get'
  })
}

export function getReportePorConsecutivo(reti_id, repo_consecutivo) {
  const data = {
    reti_id,
    repo_consecutivo
  }
  return request({
    url: '/repo/gbtc/' + data.reti_id + '/' + data.repo_consecutivo,
    method: 'get'
  })
}

export function buscarReportePorTipoConsectivo(tireuc_id, reti_id, repo_consecutivo) {
  const data = {
    tireuc_id,
    reti_id,
    repo_consecutivo
  }
  return request({
    url: '/repo/btc/' + data.tireuc_id + '/' + data.reti_id + '/' + data.repo_consecutivo,
    method: 'get'
  })
}

export function buscarReportePorVarios(filtro, tireuc_id) {
  const data = {
    filtro,
    tireuc_id
  }
  return request({
    url: '/repo/vari/' + data.filtro + '/' + data.tireuc_id,
    method: 'get'
  })
}

export function buscarReporteMigracionPorVarios(filtro, tireuc_id) {
  const data = {
    filtro,
    tireuc_id
  }
  return request({
    url: '/repo/varim/' + data.filtro + '/' + data.tireuc_id,
    method: 'get'
  })
}

export function saveReporte(reporte) {
  const data = {
    reporte
  }
  return request({
    url: '/repo/save',
    method: 'post',
    data: data.reporte
  })
}

export function updateReporte(reporte) {
  const data = {
    reporte
  }
  return request({
    url: '/repo/upd',
    method: 'post',
    data: data.reporte
  })
}

export function updateReporteElemento(reporte) {
  const data = {
    reporte
  }
  return request({
    url: '/repo/upde',
    method: 'post',
    data: data.reporte
  })
}

export function updateReporteParcial(reporte) {
  const data = {
    reporte
  }
  return request({
    url: '/repo/pupd',
    method: 'post',
    data: data.reporte
  })
}

export function deleteReporte(repo_id) {
  const data = {
    repo_id
  }
  return request({
    url: '/repo/del/' + data.repo_id,
    method: 'get'
  })
}

export function convertirReporte(repo_id) {
  const data = {
    repo_id
  }
  return request({
    url: '/repo/conv/' + data.repo_id,
    method: 'get'
  })
}

export function validarCodigo(elem_id, codigo) {
  const data = {
    elem_id,
    codigo
  }
  return request({
    url: '/repo/vali/' + data.elem_id + '/' + data.codigo,
    method: 'get'
  })
}

export function validarReporteDiligenciado(reti_id, repo_consecutivo) {
  const data = {
    reti_id,
    repo_consecutivo
  }
  return request({
    url: '/repo/vadi/' + data.reti_id + '/' + data.repo_consecutivo,
    method: 'get'
  })
}

export function reporteSinOt(fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/repo/rsino/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function actualizarHistoria() {
  return request({
    url: '/repo/updh',
    method: 'get'
  })
}

export function getEstados() {
  return request({
    url: '/repo/st/get',
    method: 'get'
  })
}

export function getTipos() {
  return request({
    url: '/repo/tp/get',
    method: 'get'
  })
}

export function getTiposMigracion() {
  return request({
    url: '/repo/tpm/get',
    method: 'get'
  })
}

export function printReporte(repo_id, empr_id) {
  const data = {
    repo_id,
    empr_id
  }
  const url =
    window.location.protocol +
    '//' +
    window.location.host.split('/')[0] +
    '/api' +
    '/repo/prn/repo/' +
    data.repo_id +
    '/' +
    data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function printReporteRelacion(
  fecha_inicial,
  fecha_final,
  empr_id,
  usua_id,
  formato
) {
  const data = {
    fecha_inicial,
    fecha_final,
    empr_id,
    usua_id,
    formato
  }
  return request({
    url:
    window.location.protocol +
    '//' +
    window.location.host.split('/')[0] +
    '/api' +
    '/repo/prn/rela/' +
    data.fecha_inicial +
    '/' +
    data.fecha_final +
    '/' +
    data.empr_id +
    '/' +
    usua_id +
    '/' +
    formato,
    method: 'get',
    responseType: 'blob'
  })
}

export function printReporteRelacionFiltrado(
  cuad_id,
  fecha_inicial,
  fecha_final,
  empr_id,
  usua_id,
  formato
) {
  const data = {
    cuad_id,
    fecha_inicial,
    fecha_final,
    empr_id,
    usua_id,
    formato
  }
  return request({
    url:
    window.location.protocol +
    '//' +
    window.location.host.split('/')[0] +
    '/api' +
    '/repo/prn/relaf/' +
    data.cuad_id +
    '/' +
    data.fecha_inicial +
    '/' +
    data.fecha_final +
    '/' +
    data.empr_id +
    '/' +
    usua_id +
    '/' +
    formato,
    method: 'get',
    responseType: 'blob'
  })
}

export function printReporteEjecutado(
  fecha_inicial,
  fecha_final,
  empr_id,
  usua_id,
  formato
) {
  const data = {
    fecha_inicial,
    fecha_final,
    empr_id,
    usua_id,
    formato
  }
  return request({
    url:
      '/repo/prn/ejec/' +
      data.fecha_inicial +
      '/' +
      data.fecha_final +
      '/' +
      data.empr_id +
      '/' +
      usua_id +
      '/' +
      formato,
    method: 'get',
    responseType: 'blob'
  })
}

export function printReporteEjecutadoFiltrado(
  cuad_id,
  fecha_inicial,
  fecha_final,
  empr_id,
  usua_id,
  formato
) {
  const data = {
    cuad_id,
    fecha_inicial,
    fecha_final,
    empr_id,
    usua_id,
    formato
  }
  return request({
    url:
      '/repo/prn/ejecf/' +
      data.cuad_id + '/' +
      data.fecha_inicial +
      '/' +
      data.fecha_final +
      '/' +
      data.empr_id +
      '/' +
      usua_id +
      '/' +
      formato,
    method: 'get',
    responseType: 'blob'
  })
}

export function printReporteBlanco(reti_id, empr_id) {
  const data = {
    reti_id,
    empr_id
  }
  const url =
    window.location.protocol +
    '//' +
    window.location.host.split('/')[0] +
    '/api' +
    '/repo/prn/form/' +
    data.reti_id +
    '/' +
    data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function getActaDesmonteXls(fecha_corte, tireuc_id) {
  const data = {
    fecha_corte,
    tireuc_id
  }
  return request({
    url: '/repo/acde/' + data.fecha_corte + '/' + data.tireuc_id,
    method: 'get',
    responseType: 'blob'
  })
}

export function getFoto(path) {
  return request({
    url: '/repo/gfd/' + path,
    method: 'get'
  })
}

export function getReportesParaCierreDirecto(fi, ff) {
  return request({
    url: '/repo/grcd/' + fi + '/' + ff,
    method: 'get'
  })
}

export function cerrarReporte(tireuc_id, repo_id) {
  return request({
    url: '/repo/cr/' + tireuc_id + '/' + repo_id,
    method: 'get'
  })
}

export function cerrarReportes(lista) {
  return request({
    url: '/repo/crs',
    method: 'post',
    data: lista
  })
}

export function siap_informe_material_usado_por_reti(fi, ff) {
  return request({
    url: '/repo/inmu/' + fi + '/' + ff,
    method: 'get'
  })
}

export function siap_informe_material_usado_por_reti_xlsx(fi, ff) {
  return request({
    url: '/repo/inmuxlsx/' + fi + '/' + ff,
    method: 'get',
    responseType: 'blob'
  })
}
