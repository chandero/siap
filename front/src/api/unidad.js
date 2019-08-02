import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/unid/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getUnidadesTodas() {
  return request({
    url: '/unid/all',
    method: 'get'
  })
}

export function getUnidad(unid_id) {
  const data = {
    unid_id
  }
  return request({
    url: '/unid/' + data.unid_id,
    method: 'get'
  })
}

export function saveUnidad(unidad) {
  const data = {
    unidad
  }
  return request({
    url: '/unid/save',
    method: 'post',
    data: data.unidad
  })
}

export function updateUnidad(unidad) {
  const data = {
    unidad
  }
  return request({
    url: '/unid/upd',
    method: 'post',
    data: data.unidad
  })
}

export function deleteUnidad(unid_id) {
  const data = {
    unid_id
  }
  return request({
    url: '/unid/del/' + data.unid_id,
    method: 'get'
  })
}
