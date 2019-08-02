import request from '@/utils/request'

export function getTodos(page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/aap/get',
    method: 'post',
    data: data
  })
}

export function getAaps(filter) {
  const data = {
    filter
  }
  return request({
    url: '/aap/all',
    method: 'post',
    data: data
  })
}

export function getAap(aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/aap/' + data.aap_id,
    method: 'get'
  })
}

export function getAapValidar(aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/aap/vali/' + data.aap_id,
    method: 'get'
  })
}

export function getAapEdit(aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/aap/edit/' + data.aap_id,
    method: 'get'
  })
}

export function getAapApoyo(aap_apoyo) {
  const data = {
    aap_apoyo
  }
  return request({
    url: '/aap/apoyo/' + data.aap_apoyo,
    method: 'get'
  })
}

export function setAap(aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/aap/set/' + data.aap_id,
    method: 'get'
  })
}

export function saveAap(aap) {
  const data = {
    aap
  }
  return request({
    url: '/aap/save',
    method: 'post',
    data: data.aap
  })
}

export function updateAap(aap) {
  const data = {
    aap
  }
  return request({
    url: '/aap/upd',
    method: 'post',
    data: data.aap
  })
}

export function deleteAap(aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/aap/del/' + data.aap_id,
    method: 'get'
  })
}

export function buscarSiguiente() {
  return request({
    url: '/aap/next',
    method: 'get'
  })
}

export function codigo(tipo) {
  const data = {
    tipo
  }
  return request({
    url: '/coau/code/' + data.tipo,
    method: 'get'
  })
}

export function validar(tipo, codigo) {
  const data = {
    tipo,
    codigo
  }
  return request({
    url: '/coau/vaco/' + data.tipo + '/' + data.codigo,
    method: 'get'
  })
}

export function buscarPorMaterial(codigo, tiel_id) {
  const data = {
    codigo,
    tiel_id
  }
  return request({
    url: '/aap/bpm/' + data.codigo + '/' + data.tiel_id,
    method: 'get'
  })
}
