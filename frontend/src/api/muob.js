import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/muob/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getMuobs () {
  return request({
    url: '/muob/all',
    method: 'get'
  })
}

export function getMuob (muob_id) {
  const data = {
    muob_id
  }
  return request({
    url: '/muob/' + data.muob_id,
    method: 'get'
  })
}

export function getMedidorByDescripcion (query) {
  const data = {
    query
  }
  return request({
    url: '/muob/bdesc/' + data.query,
    method: 'get'
  })
}

export function saveMuob (muob) {
  const data = {
    muob
  }
  return request({
    url: '/muob/save',
    method: 'post',
    data: data.muob
  })
}

export function updateMuob (muob) {
  const data = {
    muob
  }
  return request({
    url: '/muob/upd',
    method: 'post',
    data: data.muob
  })
}

export function deleteMuob (muob_id) {
  const data = {
    muob_id
  }
  return request({
    url: '/muob/del/' + data.muob_id,
    method: 'get'
  })
}

export function informe_siap_detallado_material_muob_xls (muob_consecutivo) {
  const data = {
    muob_consecutivo
  }
  return request({
    url: '/info/isdmmx/' + data.muob_consecutivo,
    method: 'get'
  })
}
