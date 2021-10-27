import request from '@/utils/request'

export function generar (anho, mes, tireuc_id, reti_id, cotr_consecutivo) {
  return request({
    url: `/cotr/gen/${anho}/${mes}/${tireuc_id}/${reti_id}/${cotr_consecutivo}`,
    method: 'get'
  })
}

export function obtener (reti_id) {
  return request({
    url: `cotr/get/${reti_id}`,
    method: 'get'
  })
}

export function xls (cotr_id) {
  return request({
    url: `cotr/prn/${cotr_id}`,
    method: 'get',
    responseType: 'blob'
  })
}
