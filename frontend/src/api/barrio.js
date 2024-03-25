import request from '@/utils/request'

export function getBarrio (barr_id) {
  const data = {
    barr_id
  }
  return request({
    url: '/barr/byid/' + data.barr_id,
    method: 'get'
  })
}

export function getBarrios (muni_id, page_size, current_page) {
  const data = {
    muni_id,
    page_size,
    current_page
  }
  return request({
    url: '/barr/get/' + data.muni_id + '/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getBarriosEmpresa () {
  return request({
    url: '/barr/web/get/empr',
    method: 'get'
  })
}

export function saveBarrio (barrio) {
  const data = {
    barrio
  }
  return request({
    url: '/barr/save',
    method: 'post',
    data: data.barrio
  })
}

export function updateBarrio (barrio) {
  const data = {
    barrio
  }
  return request({
    url: '/barr/upd',
    method: 'post',
    data: data.barrio
  })
}

export function deleteBarrio (barr_id) {
  const data = {
    barr_id
  }
  return request({
    url: '/barr/del/' + data.barr_id,
    method: 'get'
  })
}

export function getTipoBarrio () {
  return request({
    url: '/tipobarr/get',
    method: 'get'
  })
}
