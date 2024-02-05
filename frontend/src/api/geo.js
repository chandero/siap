import request from '@/utils/request'

export function getGeoreferenciaLuminarias(barr_id, aap_tecnologia, aap_potencia, aap_id) {
  return request({
    url: `/geo/1/${barr_id}/${aap_tecnologia}/${aap_potencia}/${aap_id}`,
    method: 'get'
  })
}
