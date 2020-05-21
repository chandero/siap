import request from '@/utils/request'

export function getTodos(page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/trre/get',
    method: 'post',
    data: data
  })
}

export function getReportes() {
  return request({
    url: '/trre/all',
    method: 'get'
  })
}

export function siap_reporte_vencido() {
  return request({
    url: '/trre/venc/get',
    method: 'get'
  })
}

export function getReportesRango(anho, mes) {
  const data = {
    anho,
    mes
  }
  return request({
    url: '/trre/rang/' + data.anho + '/' + data.mes,
    method: 'get'
  })
}

export function getReporte(repo_id) {
  const data = {
    repo_id
  }
  return request({
    url: '/trre/' + data.repo_id,
    method: 'get'
  })
}

export function getReportePorConsecutivo(reti_id, repo_consecutivo) {
  const data = {
    reti_id,
    repo_consecutivo
  }
  return request({
    url: '/trre/gbtc/' + data.reti_id + '/' + data.repo_consecutivo,
    method: 'get'
  })
}

export function saveReporte(reporte) {
  const data = {
    reporte
  }
  return request({
    url: '/trre/save',
    method: 'post',
    data: data.reporte
  })
}

export function updateReporte(reporte) {
  const data = {
    reporte
  }
  return request({
    url: '/trre/upd',
    method: 'post',
    data: data.reporte
  })
}

export function deleteReporte(repo_id) {
  const data = {
    repo_id
  }
  return request({
    url: '/trre/del/' + data.repo_id,
    method: 'get'
  })
}

export function validarCodigo(elem_id, codigo) {
  const data = {
    elem_id,
    codigo
  }
  return request({
    url: '/trre/vali/' + data.elem_id + '/' + data.codigo,
    method: 'get'
  })
}

export function validarReporteDiligenciado(reti_id, repo_consecutivo) {
  const data = {
    reti_id,
    repo_consecutivo
  }
  return request({
    url: '/trre/vadi/' + data.reti_id + '/' + data.repo_consecutivo,
    method: 'get'
  })
}

export function actualizarHistoria() {
  return request({
    url: '/trre/updh',
    method: 'get'
  })
}

export function convertirReporte(repo_id, reti_id) {
  const data = {
    repo_id,
    reti_id
  }
  return request({
    url: '/trre/conv/' + data.repo_id + '/' + reti_id,
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

export function printReporte(repo_id, empr_id) {
  const data = {
    repo_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0].split(':')[0] + '/api' + '/trre/prn/repo/' + data.repo_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function printReporteRelacion(fecha_inicial, fecha_final, empr_id, usua_id, tipo) {
  const data = {
    fecha_inicial,
    fecha_final,
    empr_id,
    usua_id,
    tipo
  }
  var target
  if (data.tipo === 'pdf') {
    target = '_blank'
  } else {
    target = '_self'
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0].split(':')[0] + '/api' + '/trre/prn/rela/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.empr_id + '/' + usua_id + '/' + tipo
  window.open(url, target, 'location=no, menubar=no')
}

export function printReporteBlanco(reti_id, empr_id) {
  const data = {
    reti_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0].split(':')[0] + '/api' + '/trre/prn/form/' + data.reti_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}
