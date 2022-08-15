import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/tireuc/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getTiposReporteUcap () {
  return request({
    url: '/tireuc/all',
    method: 'get'
  })
}

export function getTipoReporteUcap (tireuc_id) {
  const data = {
    tireuc_id
  }
  return request({
    url: '/tireuc/' + data.tireuc_id,
    method: 'get'
  })
}

export function saveTipoReporteUcap (tireuc) {
  const data = {
    tireuc
  }
  return request({
    url: '/tireuc/save',
    method: 'post',
    data: data.tireuc_id
  })
}

export function updateTipoReporteUcap (tireuc_id) {
  const data = {
    tireuc_id
  }
  return request({
    url: '/tireuc/upd',
    method: 'post',
    data: data.tireuc_id
  })
}

export function deleteTipoReporteUcap (tireuc_id) {
  const data = {
    tireuc_id
  }
  return request({
    url: '/tireuc/del/' + data.tireuc_id,
    method: 'get'
  })
}
