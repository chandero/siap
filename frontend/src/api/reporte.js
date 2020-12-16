import request from '@/utils/request'

export function getTodos (page_size, current_page, orderby, filter) {
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

export function getReportes () {
  return request({
    url: '/repo/all',
    method: 'get'
  })
}

export function getReportesPorAap (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/repo/gbaap/' + data.aap_id,
    method: 'get'
  })
}

export function siap_reporte_vencido () {
  return request({
    url: '/repo/venc/get',
    method: 'get'
  })
}

export function getReportesRango (anho, mes, tireuc_id) {
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

export function getReporte (repo_id) {
  const data = {
    repo_id
  }
  return request({
    url: '/repo/' + data.repo_id,
    method: 'get'
  })
}

export function getReportePorConsecutivo (reti_id, repo_consecutivo) {
  const data = {
    reti_id,
    repo_consecutivo
  }
  return request({
    url: '/repo/gbtc/' + data.reti_id + '/' + data.repo_consecutivo,
    method: 'get'
  })
}

export function saveReporte (reporte) {
  const data = {
    reporte
  }
  return request({
    url: '/repo/save',
    method: 'post',
    data: data.reporte
  })
}

export function updateReporte (reporte) {
  const data = {
    reporte
  }
  return request({
    url: '/repo/upd',
    method: 'post',
    data: data.reporte
  })
}

export function updateReporteParcial (reporte) {
  const data = {
    reporte
  }
  return request({
    url: '/repo/pupd',
    method: 'post',
    data: data.reporte
  })
}

export function deleteReporte (repo_id) {
  const data = {
    repo_id
  }
  return request({
    url: '/repo/del/' + data.repo_id,
    method: 'get'
  })
}

export function convertirReporte (repo_id) {
  const data = {
    repo_id
  }
  return request({
    url: '/repo/conv/' + data.repo_id,
    method: 'get'
  })
}

export function validarCodigo (elem_id, codigo) {
  const data = {
    elem_id,
    codigo
  }
  return request({
    url: '/repo/vali/' + data.elem_id + '/' + data.codigo,
    method: 'get'
  })
}

export function validarReporteDiligenciado (reti_id, repo_consecutivo) {
  const data = {
    reti_id,
    repo_consecutivo
  }
  return request({
    url: '/repo/vadi/' + data.reti_id + '/' + data.repo_consecutivo,
    method: 'get'
  })
}

export function reporteSinOt (fecha_inicial, fecha_final) {
  const data = {
    fecha_inicial,
    fecha_final
  }
  return request({
    url: '/repo/rsino/' + data.fecha_inicial + '/' + data.fecha_final,
    method: 'get'
  })
}

export function actualizarHistoria () {
  return request({
    url: '/repo/updh',
    method: 'get'
  })
}

export function getEstados () {
  return request({
    url: '/repo/st/get',
    method: 'get'
  })
}

export function getTipos () {
  return request({
    url: '/repo/tp/get',
    method: 'get'
  })
}

export function printReporte (repo_id, empr_id) {
  const data = {
    repo_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/repo/prn/repo/' + data.repo_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function printReporteRelacion (fecha_inicial, fecha_final, empr_id, usua_id, formato, tipo) {
  const data = {
    fecha_inicial,
    fecha_final,
    empr_id,
    usua_id,
    formato,
    tipo
  }
  var target
  if (data.tipo === 'pdf') {
    target = '_blank'
  } else {
    target = '_self'
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/repo/prn/rela/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.empr_id + '/' + usua_id + '/' + formato + '/' + tipo
  window.open(url, target, 'location=no, menubar=no')
}

export function printReporteBlanco (reti_id, empr_id) {
  const data = {
    reti_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/repo/prn/form/' + data.reti_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}
