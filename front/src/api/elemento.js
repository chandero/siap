import request from '@/utils/request'

export function getTodos(page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/elem/get',
    method: 'post',
    data: data
  })
}

export function getElementos() {
  return request({
    url: '/elem/all',
    method: 'get'
  })
}

export function getElemento(elem_id) {
  const data = {
    elem_id
  }
  return request({
    url: '/elem/' + data.elem_id,
    method: 'get'
  })
}

export function getElementoByDescripcion(query) {
  const data = {
    query
  }
  return request({
    url: '/elem/bdesc/' + data.query,
    method: 'get'
  })
}

export function getElementoByCode(code) {
  const data = {
    code
  }
  return request({
    url: '/elem/bcode/' + data.code,
    method: 'get'
  })
}

export function saveElemento(elemento) {
  const data = {
    elemento
  }
  return request({
    url: '/elem/save',
    method: 'post',
    data: data.elemento
  })
}

export function updateElemento(elemento) {
  const data = {
    elemento
  }
  return request({
    url: '/elem/upd',
    method: 'post',
    data: data.elemento
  })
}

export function deleteElemento(elem_id) {
  const data = {
    elem_id
  }
  return request({
    url: '/elem/del/' + data.elem_id,
    method: 'get'
  })
}

export function updatePriceElemento(elem_id, elpr_anho, elpr_precio) {
  const data = {
    elem_id,
    elpr_anho,
    elpr_precio
  }
  return request({
    url: '/elem/upa',
    method: 'post',
    data: data
  })
}
