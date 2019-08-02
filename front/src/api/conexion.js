import request from '@/utils/request'

export function getConexiones() {
  return request({
    url: '/cone/get/all',
    method: 'get'
  })
}
