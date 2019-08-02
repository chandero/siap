import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/empr/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getEmpresas() {
  return request({
    url: '/empr/get/u',
    method: 'get'
  })
}

export function getEmpresa(empr_id) {
  const data = {
    empr_id
  }
  return request({
    url: '/empr/' + data.empr_id,
    method: 'get'
  })
}

export function getEmpresaInfo() {
  return request({
    url: '/e/ei',
    method: 'get'
  })
}

export function setEmpresa(empr_id) {
  const data = {
    empr_id
  }
  return request({
    url: '/empr/set/' + data.empr_id,
    method: 'get'
  })
}

export function saveEmpresa(empresa) {
  const data = {
    empresa
  }
  return request({
    url: '/empr/save',
    method: 'post',
    data: data.empresa
  })
}

export function updateEmpresa(empresa) {
  const data = {
    empresa
  }
  return request({
    url: '/empr/upd',
    method: 'post',
    data: data.empresa
  })
}

export function deleteEmpresa(empr_id) {
  const data = {
    empr_id
  }
  return request({
    url: '/empr/del/' + data.empr_id,
    method: 'get'
  })
}
