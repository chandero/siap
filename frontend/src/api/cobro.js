import request from '@/utils/request'

export function generar (anho, mes, tireuc_id, reti_id) {
  return request({
    url: `/cotr/gen/${anho}/${mes}/${tireuc_id}/${reti_id}`,
    method: 'get'
  })
}
