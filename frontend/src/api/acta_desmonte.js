import request from '@/utils/request'

export function getTodos () {
  return request({
    url: '/acde/get',
    method: 'get'
  })
}

export function generarActas(fi, ff, tu) {
  return request({
    url: `/acde/gen/${fi}/${ff}/${tu}`,
    method: 'get'
  })
}

export function getActa(id) {
  return request({
    url: `/acde/get/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}
