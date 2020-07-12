import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/orig/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getOrigenes() {
  return request({
    url: '/orig/all',
    method: 'get'
  })
}

export function getOrigen(orig_id) {
  const data = {
    orig_id
  }
  return request({
    url: '/orig/' + data.orig_id,
    method: 'get'
  })
}

export function saveOrigen(origen) {
  const data = {
    origen
  }
  return request({
    url: '/orig/save',
    method: 'post',
    data: data.origen
  })
}

export function updateOrigen(origen) {
  const data = {
    origen
  }
  return request({
    url: '/orig/upd',
    method: 'post',
    data: data.origen
  })
}

export function deleteOrigen(orig_id) {
  const data = {
    orig_id
  }
  return request({
    url: '/orig/del/' + data.orig_id,
    method: 'get'
  })
}
