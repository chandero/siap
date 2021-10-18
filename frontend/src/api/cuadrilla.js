import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/cuad/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getCuadrillas () {
  return request({
    url: '/cuad/get/all',
    method: 'get'
  })
}

export function getCuadrilla (cuad_id) {
  const data = {
    cuad_id
  }
  return request({
    url: '/cuad/' + data.cuad_id,
    method: 'get'
  })
}

export function saveCuadrilla (cuadrilla) {
  const data = {
    cuadrilla
  }
  return request({
    url: '/cuad/save',
    method: 'post',
    data: data.cuadrilla
  })
}

export function updateCuadrilla (cuadrilla) {
  const data = {
    cuadrilla
  }
  return request({
    url: '/cuad/upd',
    method: 'post',
    data: data.cuadrilla
  })
}

export function deleteCuadrilla (cuad_id) {
  const data = {
    cuad_id
  }
  return request({
    url: '/cuad/del/' + data.cuad_id,
    method: 'get'
  })
}
