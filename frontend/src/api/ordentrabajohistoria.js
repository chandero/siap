import request from '@/utils/request'

export function getOrders() {
  return request({
    url: '/orhi/all',
    method: 'get'
  })
}

export function saveOrder(order) {
  return request({
    url: '/orhi/save',
    data: order
  })
}

export function deleteOrder(orhi_id) {
  return request({
    url: `orhi/del/${orhi_id}`
  })
}
