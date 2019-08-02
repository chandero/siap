import request from '@/utils/request'

export function getTodos(filtro, page_size, current_page) {
  const data = {
    filtro,
    page_size,
    current_page
  }
  return request({
    url: '/elem/get/' + data.filtro + '/' + data.page_size + '/' + data.current_page,
    method: 'get'
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
