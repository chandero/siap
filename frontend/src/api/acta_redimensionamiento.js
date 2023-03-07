import request from '@/utils/request'

export function acreTodas(page_size, current_page, orderby, filter) {
  return request({
    url: 'acre/all',
    method: 'post',
    data: {
      page_size,
      current_page,
      orderby,
      filter
    }
  })
}

export function acreActa(anho, periodo) {
  return request({
    url: `/acre/acta/${anho}/${periodo}`,
    method: 'get'
  })
}

export function acreAnexo(anho, periodo) {
  return request({
    url: `/acre/acan/${anho}/${periodo}`,
    method: 'get'
  })
}

export function acreCrear(anho, periodo) {
  return request({
    url: `/acre/acta/${anho}/${periodo}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function acreReprocesar(anho, periodo) {
  return request({
    url: `/acre/reprocesar/${anho}/${periodo}`,
    method: 'get'
  })
}
