
import request from '@/utils/request'
export function apiKey() {
  return request({
    url: '/google/controller/key',
    method: 'get'
  })
}
