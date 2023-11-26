import request from '@/utils/request'

export function getGeoreferenciaLuminarias() {
  return request({
    url: '/geo/1',
    method: 'get'
  })
}
