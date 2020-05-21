import request from '@/utils/request'

export function getTodos(page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/maob/get',
    method: 'post',
    data: data
  })
}

export function getManoObras() {
  return request({
    url: '/maob/all',
    method: 'get'
  })
}

export function getManoObra(elem_id) {
  const data = {
    elem_id
  }
  return request({
    url: '/maob/' + data.elem_id,
    method: 'get'
  })
}

export function getManoObraByDescripcion(query) {
  const data = {
    query
  }
  return request({
    url: '/maob/bdesc/' + data.query,
    method: 'get'
  })
}

export function saveManoObra(manoobra) {
  const data = {
    manoobra
  }
  return request({
    url: '/maob/save',
    method: 'post',
    data: data.manoobra
  })
}

export function updateManoObra(manoobra) {
  const data = {
    manoobra
  }
  return request({
    url: '/maob/upd',
    method: 'post',
    data: data.manoobra
  })
}

export function deleteManoObra(elem_id) {
  const data = {
    elem_id
  }
  return request({
    url: '/maob/del/' + data.elem_id,
    method: 'get'
  })
}
