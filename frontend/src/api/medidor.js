import request from '@/utils/request'

export function getAap (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/medi/' + data.aap_id,
    method: 'get'
  })
}

export function getAapValidar (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/medi/vali/' + data.aap_id,
    method: 'get'
  })
}

export function getAapEdit (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/medi/edit/' + data.aap_id,
    method: 'get'
  })
}

export function getAapEditByNumber (aap_numero) {
  const data = {
    aap_numero
  }
  return request({
    url: '/medi/edit2/' + data.aap_numero,
    method: 'get'
  })
}

export function buscarSiguiente () {
  return request({
    url: '/medi/next',
    method: 'get'
  })
}

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/medi/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getMedidors () {
  return request({
    url: '/medi/all',
    method: 'get'
  })
}

export function getMedidorTablaDato () {
  return request({
    url: '/medi/gmtd',
    method: 'get'
  })
}

export function getMedidor (medi_id) {
  const data = {
    medi_id
  }
  return request({
    url: '/medi/' + data.medi_id,
    method: 'get'
  })
}

export function getMedidorByDescripcion (query) {
  const data = {
    query
  }
  return request({
    url: '/medi/bdesc/' + data.query,
    method: 'get'
  })
}

export function saveMedidor (mediento) {
  const data = {
    mediento
  }
  return request({
    url: '/medi/save',
    method: 'post',
    data: data.mediento
  })
}

export function updateMedidor (mediento) {
  const data = {
    mediento
  }
  return request({
    url: '/medi/upd',
    method: 'post',
    data: data.mediento
  })
}

export function deleteMedidor (medi_id) {
  const data = {
    medi_id
  }
  return request({
    url: '/medi/del/' + data.medi_id,
    method: 'get'
  })
}

export function informe_siap_medidor (empr_id) {
  const data = {
    empr_id
  }
  return request({
    url: '/medi/ism/' + data.empr_id,
    method: 'get'
  })
}
