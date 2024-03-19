
import request from '@/utils/request'
export function apiKey() {
  return request({
    url: '/geo/key',
    method: 'get'
  })
}
