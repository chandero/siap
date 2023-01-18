import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/cara/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getCaracteristicas () {
  return request({
    url: '/cara/get/all',
    method: 'get'
  })
}

export function getCaracteristica (cara_id) {
  const data = {
    cara_id
  }
  return request({
    url: '/cara/' + data.cara_id,
    method: 'get'
  })
}

export function saveCaracteristica (caracteristica) {
  const data = {
    caracteristica
  }
  return request({
    url: '/cara/save',
    method: 'post',
    data: data.caracteristica
  })
}

export function updateCaracteristica (caracteristica) {
  const data = {
    caracteristica
  }
  return request({
    url: '/cara/upd',
    method: 'post',
    data: data.caracteristica
  })
}

export function deleteCaracteristica (cara_id) {
  const data = {
    cara_id
  }
  return request({
    url: '/cara/del/' + data.cara_id,
    method: 'get'
  })
}
