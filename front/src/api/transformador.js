import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/tran/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getTransformadors() {
  return request({
    url: '/tran/all',
    method: 'get'
  })
}

export function getTransformador(tran_id) {
  const data = {
    tran_id
  }
  return request({
    url: '/tran/' + data.tran_id,
    method: 'get'
  })
}

export function getTransformadorByDescripcion(query) {
  const data = {
    query
  }
  return request({
    url: '/tran/bdesc/' + data.query,
    method: 'get'
  })
}

export function saveTransformador(mediento) {
  const data = {
    mediento
  }
  return request({
    url: '/tran/save',
    method: 'post',
    data: data.mediento
  })
}

export function updateTransformador(mediento) {
  const data = {
    mediento
  }
  return request({
    url: '/tran/upd',
    method: 'post',
    data: data.mediento
  })
}

export function deleteTransformador(tran_id) {
  const data = {
    tran_id
  }
  return request({
    url: '/tran/del/' + data.tran_id,
    method: 'get'
  })
}

export function informe_siap_transformador(empr_id) {
  const data = {
    empr_id
  }
  return request({
    url: '/tran/ism/' + data.empr_id,
    method: 'get'
  })
}
