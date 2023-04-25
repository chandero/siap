import request from '@/utils/request'

export function obtenerInforme(fi, ff) {
  return request({
    url: '/audi/coau/l/' + fi + '/' + ff,
    method: 'get'
  })
}
