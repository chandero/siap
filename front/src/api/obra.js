import request from '@/utils/request'

export function getTodos(page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/obra/get',
    method: 'post',
    data: data
  })
}

export function getObras() {
  return request({
    url: '/obra/all',
    method: 'get'
  })
}

export function getObrasRango(anho, mes) {
  const data = {
    anho,
    mes
  }
  return request({
    url: '/obra/rang/' + data.anho + '/' + data.mes,
    method: 'get'
  })
}

export function getObra(obra_id) {
  const data = {
    obra_id
  }
  return request({
    url: '/obra/' + data.obra_id,
    method: 'get'
  })
}

export function getObraPorConsecutivo(obra_consecutivo) {
  const data = {
    obra_consecutivo
  }
  return request({
    url: '/obra/gbtc/' + data.obra_consecutivo,
    method: 'get'
  })
}

export function saveObra(obra) {
  const data = {
    obra
  }
  return request({
    url: '/obra/save',
    method: 'post',
    data: data.obra
  })
}

export function updateObra(obra) {
  const data = {
    obra
  }
  return request({
    url: '/obra/upd',
    method: 'post',
    data: data.obra
  })
}

export function deleteObra(obra_id) {
  const data = {
    obra_id
  }
  return request({
    url: '/obra/del/' + data.obra_id,
    method: 'get'
  })
}

export function validarCodigo(elem_id, codigo) {
  const data = {
    elem_id,
    codigo
  }
  return request({
    url: '/obra/vali/' + data.elem_id + '/' + data.codigo,
    method: 'get'
  })
}

export function actualizarHistoria() {
  return request({
    url: '/obra/updh',
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
    url: '/obra/tp/get',
    method: 'get'
  })
}

export function printObra(obra_id, empr_id) {
  const data = {
    obra_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/obra/prn/obra/' + data.obra_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function printObraRelacion(fecha_inicial, fecha_final, empr_id, usua_id) {
  const data = {
    fecha_inicial,
    fecha_final,
    empr_id,
    usua_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/obra/prn/rela/' + data.fecha_inicial + '/' + data.fecha_final + '/' + data.empr_id + '/' + usua_id
  window.open(url, '_blank', 'location=no, menubar=no')
}

export function printObraBlanco(reti_id, empr_id) {
  const data = {
    reti_id,
    empr_id
  }
  const url = window.location.protocol + '//' + window.location.host.split('/')[0] + '/api' + '/obra/prn/form/' + data.reti_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no, menubar=no')
}
