import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/acti/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getActividades () {
  return request({
    url: '/acti/all',
    method: 'get'
  })
}

export function getActividad (acti_id) {
  const data = {
    acti_id
  }
  return request({
    url: '/acti/' + data.acti_id,
    method: 'get'
  })
}

export function saveActividad (actividad) {
  const data = {
    actividad
  }
  return request({
    url: '/acti/save',
    method: 'post',
    data: data.actividad
  })
}

export function updateActividad (actividad) {
  const data = {
    actividad
  }
  return request({
    url: '/acti/upd',
    method: 'post',
    data: data.actividad
  })
}

export function deleteActividad (acti_id) {
  const data = {
    acti_id
  }
  return request({
    url: '/acti/del/' + data.acti_id,
    method: 'get'
  })
}
