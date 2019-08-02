import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/soti/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getSolicitudTipos() {
  return request({
    url: '/soti/all',
    method: 'get'
  })
}

export function getSolicitudTipo(soti_id) {
  const data = {
    soti_id
  }
  return request({
    url: '/soti/' + data.soti_id,
    method: 'get'
  })
}

export function saveSolicitudTipo(st) {
  const data = {
    st
  }
  return request({
    url: '/soti/save',
    method: 'post',
    data: data.st
  })
}

export function updateSolicitudTipo(st) {
  const data = {
    st
  }
  return request({
    url: '/soti/upd',
    method: 'post',
    data: data.st
  })
}

export function deleteSolicitudTipo(soti_id) {
  const data = {
    soti_id
  }
  return request({
    url: '/soti/del/' + data.soti_id,
    method: 'get'
  })
}
