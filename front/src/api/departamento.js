import request from '@/utils/request'

export function getDepartamentos() {
  return request({
    url: '/depa/get',
    method: 'get'
  })
}
