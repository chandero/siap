import request from '@/utils/request'

export function getOrders() {
  return request({
    url: '/orhi/all',
    method: 'get'
  })
}

export function getOrdenTrabajoHistorico(orhi_id) {
  return request({
    url: `/orhi/get/${orhi_id}`,
    method: 'get'
  })
}

export function saveOrder(order) {
  return request({
    method: 'post',
    url: '/orhi/save',
    data: order
  })
}

export function deleteOrder(orhi_id) {
  return request({
    url: `orhi/del/${orhi_id}`
  })
}

export function getElementos() {
  return request({
    url: '/orhi/elementos',
    method: 'get'
  })
}

export function getElementoPrecio(elhi_id) {
  return request({
    url: `/orhi/elemento/${elhi_id}`,
    method: 'get'
  })
}

export function getElementoByDescripcion(descripcion) {
  return request({
    url: `/orhi/elemento/desc/${descripcion}`,
    method: 'get'
  })
}

export function getElementoByCode(code) {
  return request({
    url: `/orhi/elemento/code/${code}`,
    method: 'get'
  })
}
