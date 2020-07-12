import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/acci/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getAcciones() {
  return request({
    url: '/acci/all',
    method: 'get'
  })
}

export function getAccion(acci_id) {
  const data = {
    acci_id
  }
  return request({
    url: '/acci/' + data.acci_id,
    method: 'get'
  })
}

export function saveAccion(accion) {
  const data = {
    accion
  }
  return request({
    url: '/acci/save',
    method: 'post',
    data: data.accion
  })
}

export function updateAccion(accion) {
  const data = {
    accion
  }
  return request({
    url: '/acci/upd',
    method: 'post',
    data: data.accion
  })
}

export function deleteAccion(acci_id) {
  const data = {
    acci_id
  }
  return request({
    url: '/acci/del/' + data.acci_id,
    method: 'get'
  })
}
