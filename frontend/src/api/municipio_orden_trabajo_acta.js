import request from '@/utils/request'

export function getTodos(page_size, current_page, filter, orderby) {
  const data = {
    page_size,
    current_page,
    orderby
  }
  return request({
    url: '/muot/get',
    method: 'post',
    data: data
  })
}

export function getMuots() {
  return request({
    url: '/muot/all',
    method: 'get'
  })
}

export function getMuot(muot_id) {
  const data = {
    muot_id
  }
  return request({
    url: '/muot/' + data.muot_id,
    method: 'get'
  })
}

export function saveMuot(muot) {
  const data = {
    muot
  }
  return request({
    url: '/muot/save',
    method: 'post',
    data: data.muot
  })
}

export function updateMuot(muot) {
  const data = {
    muot
  }
  return request({
    url: '/muot/upd',
    method: 'post',
    data: data.muot
  })
}

export function deleteMuot(muot_id) {
  const data = {
    muot_id
  }
  return request({
    url: '/muot/del/' + data.muot_id,
    method: 'get'
  })
}

export function informe_siap_detallado_material_muot_xlsx(muot_id) {
  const data = {
    muot_id
  }
  return request({
    url: '/info/isdmmuotx/' + data.muot_id,
    method: 'get',
    responseType: 'blob'
  })
}

export function getItems(muot_id, tipo_obra_id) {
  const data = {
    muot_id,
    tipo_obra_id
  }
  return request({
    url: '/motai/get/' + data.muot_id + '/' + data.tipo_obra_id,
    method: 'get',
    data: data
  })
}

export function saveItem(item) {
  const data = {
    item
  }
  return request({
    url: '/motai/save',
    method: 'post',
    data: data.item
  })
}

export function updateItem(item) {
  const data = {
    item
  }
  return request({
    url: '/motai/upd',
    method: 'post',
    data: data.item
  })
}

export function deleteItem(item) {
  const data = {
    item
  }
  return request({
    url: '/motai/del',
    method: 'post',
    data: data.item
  })
}

export function getMuotaiByMuotId(muotai_id) {
  const data = {
    muotai_id
  }
  return request({
    url: '/motai/get/' + data.muotai_id,
    method: 'get'
  })
}
