import request from '@/utils/request'

export function getTodos (fi, ff) {
  return request({
    url: `/acin/all/${fi}/${ff}`,
    method: 'get'
  })
}

export function generarActas(anho, periodo, tarifa) {
  return request({
    url: `/acin/gen/${anho}/${periodo}/${tarifa}`,
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
