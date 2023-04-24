import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/ucap/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getUcapsTodas () {
  return request({
    url: '/ucap/all',
    method: 'get'
  })
}

export function getUcap (ucap_id) {
  const data = {
    ucap_id
  }
  return request({
    url: '/ucap/' + data.ucap_id,
    method: 'get'
  })
}

export function saveUcap (ucap) {
  const data = {
    ucap
  }
  return request({
    url: '/ucap/save',
    method: 'post',
    data: data.ucap
  })
}

export function updateUcap (ucap) {
  const data = {
    ucap
  }
  return request({
    url: '/ucap/upd',
    method: 'post',
    data: data.ucap
  })
}

export function deleteUcap (ucap_id) {
  const data = {
    ucap_id
  }
  return request({
    url: '/ucap/del/' + data.ucap_id,
    method: 'get'
  })
}

export function getListaIppIpc () {
  return request({
    url: '/ucap/lipp/all',
    method: 'get'
  })
}

export function guardarIppIpc (data) {
  return request({
    url: '/ucap/lipp/save',
    method: 'post',
    data: data
  })
}

export function eliminarIppIpc(id) {
  return request({
    url: '/ucap/lipp/del/' + id,
    method: 'get'
  })
}
