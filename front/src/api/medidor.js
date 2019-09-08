import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/medi/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getMedidors() {
  return request({
    url: '/medi/all',
    method: 'get'
  })
}

export function getMedidorTablaDato() {
  return request({
    url: '/medi/gmtd',
    method: 'get'
  })
}

export function getMedidor(medi_id) {
  const data = {
    medi_id
  }
  return request({
    url: '/medi/' + data.medi_id,
    method: 'get'
  })
}

export function getMedidorByDescripcion(query) {
  const data = {
    query
  }
  return request({
    url: '/medi/bdesc/' + data.query,
    method: 'get'
  })
}

export function saveMedidor(mediento) {
  const data = {
    mediento
  }
  return request({
    url: '/medi/save',
    method: 'post',
    data: data.mediento
  })
}

export function updateMedidor(mediento) {
  const data = {
    mediento
  }
  return request({
    url: '/medi/upd',
    method: 'post',
    data: data.mediento
  })
}

export function deleteMedidor(medi_id) {
  const data = {
    medi_id
  }
  return request({
    url: '/medi/del/' + data.medi_id,
    method: 'get'
  })
}

export function informe_siap_medidor(empr_id) {
  const data = {
    empr_id
  }
  return request({
    url: '/medi/ism/' + data.empr_id,
    method: 'get'
  })
}
