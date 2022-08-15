import request from '@/utils/request'

export function getMedioambiente () {
  return request({
    url: '/meam/get',
    method: 'get'
  })
}
