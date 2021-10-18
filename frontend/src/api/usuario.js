import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/usua/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getParaCuadrilla () {
  return request({
    url: '/usua/get/cuad',
    method: 'get'
  })
}

export function getUsuario (usua_id) {
  const data = {
    usua_id
  }
  return request({
    url: '/usua/bpi/' + data.usua_id,
    method: 'get'
  })
}

export function saveUsuario (usuario) {
  const data = {
    usuario
  }
  return request({
    url: '/usua/save',
    method: 'post',
    data: data.usuario
  })
}

export function updateUsuario (usuario) {
  const data = {
    usuario
  }
  return request({
    url: '/usua/upd',
    method: 'post',
    data: data.usuario
  })
}

export function deleteUsuario (usua_id) {
  const data = {
    usua_id
  }
  return request({
    url: '/usua/del/' + data.usua_id,
    method: 'get'
  })
}

export function passwordRecovery (usua_email) {
  const data = {
    usua_email
  }
  return request({
    url: '/usua/recovery/' + data.usua_email,
    method: 'get'
  })
}

export function linkValidator (link) {
  const data = {
    link
  }
  return request({
    url: '/usua/link/' + data.link,
    method: 'get'
  })
}

export function changePassword (link, password) {
  const data = {
    link,
    password
  }
  return request({
    url: '/usua/change',
    method: 'post',
    data: { link: data.link, password: data.password }
  })
}
