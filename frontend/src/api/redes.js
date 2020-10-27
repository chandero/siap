import request from '@/utils/request'

export function getTodos (page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/redes/get',
    method: 'post',
    data: data
  })
}
export function getTodosEliminados (page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/redes/gte',
    method: 'post',
    data: data
  })
}

export function getAaps (filter) {
  const data = {
    filter
  }
  return request({
    url: '/redes/all',
    method: 'post',
    data: data
  })
}

export function getAap (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/redes/' + data.aap_id,
    method: 'get'
  })
}

export function getAapValidar (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/redes/vali/' + data.aap_id,
    method: 'get'
  })
}

export function getAapEdit (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/redes/edit/' + data.aap_id,
    method: 'get'
  })
}

export function getAapApoyo (aap_apoyo) {
  const data = {
    aap_apoyo
  }
  return request({
    url: '/redes/apoyo/' + data.aap_apoyo,
    method: 'get'
  })
}

export function setAap (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/redes/set/' + data.aap_id,
    method: 'get'
  })
}

export function saveAap (aap) {
  const data = {
    aap
  }
  return request({
    url: '/redes/save',
    method: 'post',
    data: data.aap
  })
}

export function updateAap (aap) {
  const data = {
    aap
  }
  return request({
    url: '/redes/upd',
    method: 'post',
    data: data.aap
  })
}

export function deleteAap (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/redes/del/' + data.aap_id,
    method: 'get'
  })
}

export function recuperarAap (aap_id) {
  const data = {
    aap_id
  }
  return request({
    url: '/redes/rec/' + data.aap_id,
    method: 'get'
  })
}

export function buscarSiguiente () {
  return request({
    url: '/redes/next',
    method: 'get'
  })
}

export function codigo (tipo) {
  const data = {
    tipo
  }
  return request({
    url: '/coau/code/' + data.tipo,
    method: 'get'
  })
}

export function validar (tipo, codigo) {
  const data = {
    tipo,
    codigo
  }
  return request({
    url: '/coau/vaco/' + data.tipo + '/' + data.codigo,
    method: 'get'
  })
}

export function buscarPorMaterial (codigo, tiel_id) {
  const data = {
    codigo,
    tiel_id
  }
  return request({
    url: '/redes/bpm/' + data.codigo + '/' + data.tiel_id,
    method: 'get'
  })
}

export function informe_siap_redes (empr_id) {
  const data = {
    empr_id
  }
  return request({
    url: '/redes/ism/' + data.empr_id,
    method: 'get'
  })
}