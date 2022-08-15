import request from '@/utils/request'

export function getUnitarios (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/unit/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getUnitariosTodas () {
  return request({
    url: '/unit/all',
    method: 'get'
  })
}

export function getUnitario (unit_id) {
  const data = {
    unit_id
  }
  return request({
    url: '/unit/' + data.unit_id,
    method: 'get'
  })
}

export function saveUnitario (unitario) {
  const data = {
    unitario
  }
  return request({
    url: '/unit/save',
    method: 'post',
    data: data.unitario
  })
}

export function updateUnitario (unitario) {
  const data = {
    unitario
  }
  return request({
    url: '/unit/upd',
    method: 'post',
    data: data.unitario
  })
}

export function deleteUnitario (unit_id) {
  const data = {
    unit_id
  }
  return request({
    url: '/unit/del/' + data.unit_id,
    method: 'get'
  })
}

export function unitarioMaterial () {
  return request({
    url: '/unit/elems',
    method: 'get'
  })
}
