import request from '@/utils/request'

export function getTodos (page_size, current_page, orderby, filter) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter
  }
  return request({
    url: '/ortr/get',
    method: 'post',
    data: data
  })
}

export function getOrdenes () {
  return request({
    url: '/ortr/all',
    method: 'get'
  })
}

export function getOrden (ortr_id) {
  const data = {
    ortr_id
  }
  return request({
    url: '/ortr/' + data.ortr_id,
    method: 'get'
  })
}

export function getOrdenByCuadrillaFecha(cuad_id, fecha) {
  return request({
    url: `/ortr/gbycf/${cuad_id}/${fecha}`,
    method: 'get'
  })
}

export function addReporteAOrden (ot, rp, tu) {
  const data = {
    ot,
    rp,
    tu
  }
  return request({
    url: '/otar/' + data.ot + '/' + data.rp + '/' + data.tu,
    method: 'get'
  })
}

export function saveOrden (ordentrabajo) {
  const data = {
    ordentrabajo
  }
  return request({
    url: '/ortr/save',
    method: 'post',
    data: data.ordentrabajo
  })
}

export function updateOrden (ordentrabajo) {
  const data = {
    ordentrabajo
  }
  return request({
    url: '/ortr/upd',
    method: 'post',
    data: data.ordentrabajo
  })
}

export function deleteOrden (ortr_id) {
  const data = {
    ortr_id
  }
  return request({
    url: '/ortr/del/' + data.ortr_id,
    method: 'get'
  })
}

export function getEstados () {
  return request({
    url: '/ortr/st/get',
    method: 'get'
  })
}

export function printOrden (ortr_id, empr_id) {
  const data = {
    ortr_id,
    empr_id
  }
  const url = process.env.BASE_API + '/ortr/prn/ortr/' + data.ortr_id + '/' + data.empr_id
  window.open(url, '_blank', 'location=no')
}

export function siap_informe_cambios_en_reporte(fecha_inicial, fecha_final) {
  return request({
    url: `/ortr/incb/${fecha_inicial}/${fecha_final}`,
    method: 'get',
    responseType: 'blob'
  })
}
