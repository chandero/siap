import request from '@/utils/request'

export function getTodos(page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/soli/get',
    method: 'post',
    data: data
  })
}

export function getSolicitudes() {
  return request({
    url: '/soli/all',
    method: 'get'
  })
}

export function buscarPorVencer() {
  return request({
    url: '/soli/bpv',
    method: 'get'
  })
}

export function getSolicitudesRango(anho, mes) {
  const data = {
    anho,
    mes
  }
  return request({
    url: '/soli/rang/' + data.anho + '/' + data.mes,
    method: 'get'
  })
}

export function getSolicitud(soli_id) {
  const data = {
    soli_id
  }
  return request({
    url: '/soli/' + data.soli_id,
    method: 'get'
  })
}

export function getSolicitudPorRadicado(soli_radicado) {
  const data = {
    soli_radicado
  }
  return request({
    url: '/soli/gbr/' + data.soli_radicado,
    method: 'get'
  })
}

export function saveSolicitud(solicitud) {
  const data = {
    solicitud
  }
  return request({
    url: '/soli/save',
    method: 'post',
    data: data.solicitud
  })
}

export function updateSolicitud(solicitud) {
  const data = {
    solicitud
  }
  return request({
    url: '/soli/upd',
    method: 'post',
    data: data.solicitud
  })
}

export function deleteSolicitud(soli_id) {
  const data = {
    soli_id
  }
  return request({
    url: '/soli/del/' + data.soli_id,
    method: 'get'
  })
}

export function entregarSupervisor(soli_id) {
  const data = {
    soli_id
  }
  return request({
    url: '/soli/ensu/' + data.soli_id,
    method: 'get'
  })
}

export function asignarRteSolicitud(soli_id, soli_fechaalmacen, soli_numerorte) {
  const data = {
    soli_id,
    soli_fechaalmacen,
    soli_numerorte
  }
  return request({
    url: '/soli/arte/' + data.soli_id + '/' + data.soli_fechaalmacen + '/' + data.soli_numerorte,
    method: 'get'
  })
}

export function ingresarInforme(soli_id, info) {
  const data = {
    soli_id,
    info
  }
  return request({
    url: '/soli/inin',
    method: 'post',
    data: data
  })
}

export function ingresarRespuesta(soli_id, info) {
  const data = {
    soli_id,
    info
  }
  return request({
    url: '/soli/inre',
    method: 'post',
    data: data
  })
}

export function entregarFormatoRTE(soli_id) {
  const data = {
    soli_id
  }
  return request({
    url: '/soli/enfo/' + data.soli_id,
    method: 'get'
  })
}

export function imprimirFormatoRTE(soli_id, empr_id) {
  const data = {
    soli_id,
    empr_id
  }
  const token = '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/soli/frte/' + data.soli_id + '/' + data.empr_id + '/' + token
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function imprimirRespuestaSolicitud(soli_id, empr_id, firma, editable) {
  const data = {
    soli_id,
    empr_id,
    firma,
    editable
  }
  const token = '43f44388-5cd1-4657-9f7e-ea4e014e9333'
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/soli/frs/' + data.soli_id + '/' + data.empr_id + '/' + data.firma + '/' + data.editable + '/' + token
  if (data.editable) {
    window.open(url, '_self', 'location=no, menubar=no')
  } else {
    window.open(url, '_blank', 'location=no, menubar=no')
  }
}

export function getTipos() {
  return request({
    url: '/soti/get',
    method: 'get'
  })
}

