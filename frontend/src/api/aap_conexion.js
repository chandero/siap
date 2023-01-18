import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/aaco/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getAapConexiones () {
  return request({
    url: '/aaco/all',
    method: 'get'
  })
}

export function getAapConexion (aaco_id) {
  const data = {
    aaco_id
  }
  return request({
    url: '/aaco/' + data.aaco_id,
    method: 'get'
  })
}

export function saveAapConexion (aapconexion) {
  const data = {
    aapconexion
  }
  return request({
    url: '/aaco/save',
    method: 'post',
    data: data.aapconexion
  })
}

export function updateAapConexion (aapconexion) {
  const data = {
    aapconexion
  }
  return request({
    url: '/aaco/upd',
    method: 'post',
    data: data.aapconexion
  })
}

export function deleteAapConexion (aaco_id) {
  const data = {
    aaco_id
  }
  return request({
    url: '/aaco/del/' + data.aaco_id,
    method: 'get'
  })
}
