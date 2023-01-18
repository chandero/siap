import request from '@/utils/request'

export function getTodos (page_size, current_page, orderby, filter, tipo) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter,
    tipo
  }
  return request({
    url: '/caar/get',
    method: 'post',
    data: data
  })
}

export function eliminar (anho, mes, tipo) {
  const data = {
    anho,
    mes,
    tipo
  }
  return request({
    url: '/caar/elim/' + data.anho + '/' + data.mes + '/' + data.tipo,
    method: 'get'
  })
}

export function procesar_xlsx (anho, mes, tipo, uuid) {
  const data = {
    anho,
    mes,
    tipo,
    uuid
  }
  return request({
    url: '/caar/xlsx/' + data.anho + '/' + data.mes + '/' + data.tipo + '/' + data.uuid,
    method: 'get'
  })
}
