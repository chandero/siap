import request from '@/utils/request'

export function getManoObraOrden() {
  return request({
    url: '/maher/maob/get',
    method: 'get'
  })
}

export function saveManoObraOrden(data) {
  return request({
    url: '/maher/maob/save',
    method: 'post',
    data
  })
}

export function getManoObraOrdenPrecio(page_size, current_page, orderby, filter, anho) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter,
    anho
  }
  return request({
    url: '/maher/maobpr/get',
    method: 'post',
    data
  })
}

export function saveManoObraOrdenPrecio(data) {
  return request({
    url: '/maher/maobpr/save',
    method: 'post',
    data
  })
}

export function deleteManoObraOrdenPrecio(data) {
  return request({
    url: '/maher/maobpr/del',
    method: 'post',
    data
  })
}

export function getManoObraOrdenPrecioXlsx(anho) {
  return request({
    url: '/maher/maobpr/getxlsx/' + anho,
    method: 'get',
    responseType: 'blob'
  })
}

// Mano Herramienta

export function getManoHerramientaOrden() {
  return request({
    url: '/maher/math/get',
    method: 'get'
  })
}

export function saveManoHerramientaOrden(data) {
  return request({
    url: '/maher/math/save',
    method: 'post',
    data
  })
}

export function getManoHerramientaOrdenPrecio(page_size, current_page, orderby, filter, anho) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter,
    anho
  }
  return request({
    url: '/maher/mathpr/get',
    method: 'post',
    data
  })
}

export function saveManoHerramientaOrdenPrecio(data) {
  return request({
    url: '/maher/mathpr/save',
    method: 'post',
    data
  })
}

export function deleteManoHerramientaOrdenPrecio(data) {
  return request({
    url: '/maher/mathpr/del',
    method: 'post',
    data
  })
}

export function getManoHerramientaOrdenPrecioXlsx(anho) {
  return request({
    url: '/maher/mathpr/getxlsx/' + anho,
    method: 'get',
    responseType: 'blob'
  })
}

// Mano Ingenieria

export function getManoIngenieriaOrden() {
  return request({
    url: '/maher/main/get',
    method: 'get'
  })
}

export function saveManoIngenieriaOrden(data) {
  return request({
    url: '/maher/main/save',
    method: 'post',
    data
  })
}

export function getManoIngenieriaOrdenPrecio(page_size, current_page, orderby, filter, anho) {
  const data = {
    page_size,
    current_page,
    orderby,
    filter,
    anho
  }
  return request({
    url: '/maher/mainpr/get',
    method: 'post',
    data
  })
}

export function saveManoIngenieriaOrdenPrecio(data) {
  return request({
    url: '/maher/mainpr/save',
    method: 'post',
    data
  })
}

export function getManoIngenieriaOrdenPrecioXlsx(anho) {
  return request({
    url: '/maher/mainpr/getxlsx/' + anho,
    method: 'get',
    responseType: 'blob'
  })
}
