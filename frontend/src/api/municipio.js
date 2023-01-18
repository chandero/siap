import request from '@/utils/request'

export function getMunicipios (depa_id) {
  const data = {
    depa_id
  }
  return request({
    url: '/muni/get/' + data.depa_id,
    method: 'get'
  })
}
