import request from '@/utils/request'

export function siap_grafica_reporte_pendiente() {
  return request({
    url: '/graf/sgrp',
    method: 'get'
  })
}

export function siap_grafica_reporte_vencido() {
  return request({
    url: '/graf/sgrv',
    method: 'get'
  })
}

export function siap_grafica_reporte_potencia() {
  return request({
    url: '/graf/sgrpo',
    method: 'get'
  })
}

export function siap_lista_reporte_potencia(aap_potencia) {
  const data = {
    aap_potencia
  }
  return request({
    url: '/graf/slrp/' + data.aap_potencia,
    method: 'get'
  })
}

export function siap_lista_reporte_potencia_tecnologia(aap_tecnologia, aap_potencia) {
  const data = {
    aap_tecnologia,
    aap_potencia
  }
  return request({
    url: '/graf/slrpt/' + data.aap_tecnologia + '/' + data.aap_potencia,
    method: 'get'
  })
}

export function siap_lista_reporte_potencia_medida(aaco_descripcion, aap_potencia) {
  const data = {
    aaco_descripcion,
    aap_potencia
  }
  return request({
    url: '/graf/slrpm/' + data.aaco_descripcion + '/' + data.aap_potencia,
    method: 'get'
  })
}

export function siap_lista_reporte_sector(tiba_descripcion) {
  const data = {
    tiba_descripcion
  }
  return request({
    url: '/graf/slrs/' + data.tiba_descripcion,
    method: 'get'
  })
}

export function siap_lista_reporte_uso(aaus_descripcion) {
  const data = {
    aaus_descripcion
  }
  return request({
    url: '/graf/slru/' + data.aaus_descripcion,
    method: 'get'
  })
}

export function siap_lista_reporte_tecnologia(aap_tecnologia) {
  const data = {
    aap_tecnologia
  }
  return request({
    url: '/graf/slrt/' + data.aap_tecnologia,
    method: 'get'
  })
}

export function siap_lista_reporte_medida(aaco_descripcion) {
  const data = {
    aaco_descripcion
  }
  return request({
    url: '/graf/slrm/' + data.aaco_descripcion,
    method: 'get'
  })
}

export function siap_lista_reporte_barrio(barr_descripcion) {
  const data = {
    barr_descripcion
  }
  return request({
    url: '/graf/slrb/' + data.barr_descripcion,
    method: 'get'
  })
}

export function siap_lista_potencias() {
  return request({
    url: 'graf/slp',
    method: 'get'
  })
}

export function siap_lista_tecnologias() {
  return request({
    url: 'graf/slt',
    method: 'get'
  })
}

export function siap_lista_medidas() {
  return request({
    url: 'graf/slm',
    method: 'get'
  })
}

export function siap_lista_sectores() {
  return request({
    url: 'graf/sls',
    method: 'get'
  })
}

export function siap_lista_usos() {
  return request({
    url: 'graf/slu',
    method: 'get'
  })
}

export function siap_lista_barrios() {
  return request({
    url: 'graf/slb',
    method: 'get'
  })
}

export function siap_lista_veredas() {
  return request({
    url: 'graf/slv',
    method: 'get'
  })
}

export function siap_grafica_reporte_potencia_tecnologia() {
  return request({
    url: 'graf/sgrpotec',
    method: 'get'
  })
}

export function siap_grafica_reporte_potencia_medida() {
  return request({
    url: 'graf/sgrpomed',
    method: 'get'
  })
}

export function siap_grafica_reporte_sector() {
  return request({
    url: 'graf/sgrsec',
    method: 'get'
  })
}

export function siap_grafica_reporte_uso() {
  return request({
    url: 'graf/sgruso',
    method: 'get'
  })
}

export function siap_grafica_reporte_tecnologia() {
  return request({
    url: 'graf/sgrtec',
    method: 'get'
  })
}

export function siap_grafica_reporte_medida() {
  return request({
    url: 'graf/sgrmed',
    method: 'get'
  })
}

export function siap_grafica_reporte_barrio() {
  return request({
    url: 'graf/sgrbarr',
    method: 'get'
  })
}

export function siap_grafica_reporte_vereda() {
  return request({
    url: 'graf/sgrvere',
    method: 'get'
  })
}
