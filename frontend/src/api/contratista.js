import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/cnta/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function deleteContratista(cont_id) {
  const data = {
    cont_id
  }
  return request({
    url: '/cnta/del/' + data.cont_id,
    method: 'get'
  })
}

export function saveContratista(contratista) {
  const data = {
    contratista
  }
  return request({
    url: '/cnta/save',
    method: 'post',
    data: data.contratista
  })
}

export function updateContratista(contratista) {
  const data = {
    contratista
  }
  return request({
    url: '/cnta/upd',
    method: 'post',
    data: data.contratista
  })
}

export function getContratista(cont_id) {
  const data = {
    cont_id
  }
  return request({
    url: '/cnta/' + data.cont_id,
    method: 'get'
  })
}

export function getContratistas() {
  return request({
    url: '/cnta/get/all',
    method: 'get'
  })
}
