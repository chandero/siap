import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/tiba/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getTiposBarrio() {
  return request({
    url: '/tiba/all',
    method: 'get'
  })
}

export function getTipoElemento(tiba_id) {
  const data = {
    tiba_id
  }
  return request({
    url: '/tiba/' + data.tiba_id,
    method: 'get'
  })
}

export function saveTipoElemento(tipobarrio) {
  const data = {
    tipobarrio
  }
  return request({
    url: '/tiba/save',
    method: 'post',
    data: data.tipobarrio
  })
}

export function updateTipoElemento(tipobarrio) {
  const data = {
    tipobarrio
  }
  return request({
    url: '/tiba/upd',
    method: 'post',
    data: data.tipobarrio
  })
}

export function deleteTipoElemento(tiba_id) {
  const data = {
    tiba_id
  }
  return request({
    url: '/tiba/del/' + data.tiba_id,
    method: 'get'
  })
}
