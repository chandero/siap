import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/amem/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getAapMedidorMarcas () {
  return request({
    url: '/amem/all',
    method: 'get'
  })
}

export function getAapMedidorMarca (amem_id) {
  const data = {
    amem_id
  }
  return request({
    url: '/amem/' + data.amem_id,
    method: 'get'
  })
}

export function saveAapMedidorMarca (aapmedidormarca) {
  const data = {
    aapmedidormarca
  }
  return request({
    url: '/amem/save',
    method: 'post',
    data: data.aapmedidormarca
  })
}

export function updateAapMedidorMarca (aapmedidormarca) {
  const data = {
    aapmedidormarca
  }
  return request({
    url: '/amem/upd',
    method: 'post',
    data: data.aapmedidormarca
  })
}

export function deleteAapMedidorMarca (amem_id) {
  const data = {
    amem_id
  }
  return request({
    url: '/amem/del/' + data.amem_id,
    method: 'get'
  })
}
