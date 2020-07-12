import request from '@/utils/request'

export function getTodos (page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/ortr/get',
    method: 'post',
    data: data
  })
}

export function getOrdenes () {
  return request({
    url: '/ortr/all',
    method: 'get'
  })
}

export function getOrden (ortr_id) {
  const data = {
    ortr_id
  }
  return request({
    url: '/ortr/' + data.ortr_id,
    method: 'get'
  })
}

export function saveOrden (ordentrabajo) {
  const data = {
    ordentrabajo
  }
  return request({
    url: '/ortr/save',
    method: 'post',
    data: data.ordentrabajo
  })
}

export function updateOrden (ordentrabajo) {
  const data = {
    ordentrabajo
  }
  return request({
    url: '/ortr/upd',
    method: 'post',
    data: data.ordentrabajo
  })
}

export function deleteOrden (ortr_id) {
  const data = {
    ortr_id
  }
  return request({
    url: '/ortr/del/' + data.ortr_id,
    method: 'get'
  })
}

export function getEstados () {
  return request({
    url: '/ortr/st/get',
    method: 'get'
  })
}

export function printOrden (ortr_id, empr_id) {
  const data = {
    ortr_id,
    empr_id
  }
  const url = process.env.BASE_API + '/ortr/prn/ortr/' + data.ortr_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no')
}
