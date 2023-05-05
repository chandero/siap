import request from '@/utils/request'

export function getTodos () {
  return request({
    url: '/acin/all',
    method: 'get'
  })
}

export function generarActas(anho, periodo) {
  return request({
    url: `/acin/gen/${anho}/${periodo}/`,
    method: 'get'
  })
}

export function getActa(id) {
  return request({
    url: `/acin/get/${id}`,
    method: 'get',
    responseType: 'blob'
  })
}
