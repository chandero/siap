import request from '@/utils/request'

export function generar (anho, mes, tireuc_id, reti_id, cotr_consecutivo) {
  return request({
    url: `/cotr/gen/${anho}/${mes}/${tireuc_id}/${reti_id}/${cotr_consecutivo}`,
    method: 'get'
  })
}

export function obtener (order, filter) {
  return request({
    url: 'cotr/get',
    method: 'post',
    data: { orderby: order, filter: filter }
  })
}

export function verificar (reti_id, anho, mes) {
  return request({
    url: `cotr/chk/${reti_id}/${anho}/${mes}`,
    method: 'get'
  })
}

export function consecutivo (reti_id) {
  return request({
    url: `cotr/csc/${reti_id}`,
    method: 'get'
  })
}

export function xls (cotr_id, reti_id) {
  return request({
    url: `cotr/prn/${cotr_id}/${reti_id}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function relacion (anho, periodo) {
  return request({
    url: `cotr/rel/${anho}/${periodo}`,
    method: 'get',
    responseType: 'blob'
  })
}

export function facturaTodas () {
  return request({
    url: '/cofa/all',
    method: 'get'
  })
}

export function facturaBuscar (numero) {
  return request({
    url: `/cofa/get/${numero}`,
    method: 'get'
  })
}

export function facturaCrear (data) {
  return request({
    url: '/cofa/save',
    method: 'post',
    data: data
  })
}

export function facturaActualizar (data) {
  return request({
    url: '/cofa/upd',
    method: 'post',
    data: data
  })
}

export function facturaEliminar (cofa_id) {
  return request({
    url: `/cofa/del/${cofa_id}`,
    method: 'get'
  })
}
