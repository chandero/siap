import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/tiel/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getTiposElemento () {
  return request({
    url: '/tiel/all',
    method: 'get'
  })
}

export function getTipoElemento (tiel_id) {
  const data = {
    tiel_id
  }
  return request({
    url: '/tiel/' + data.tiel_id,
    method: 'get'
  })
}

export function saveTipoElemento (tipoelemento) {
  const data = {
    tipoelemento
  }
  return request({
    url: '/tiel/save',
    method: 'post',
    data: data.tipoelemento
  })
}

export function updateTipoElemento (tipoelemento) {
  const data = {
    tipoelemento
  }
  return request({
    url: '/tiel/upd',
    method: 'post',
    data: data.tipoelemento
  })
}

export function deleteTipoElemento (tiel_id) {
  const data = {
    tiel_id
  }
  return request({
    url: '/tiel/del/' + data.tiel_id,
    method: 'get'
  })
}
