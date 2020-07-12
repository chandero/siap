import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/aamo/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getAapModelos() {
  return request({
    url: '/aamo/all',
    method: 'get'
  })
}

export function getAapModelo(aamo_id) {
  const data = {
    aamo_id
  }
  return request({
    url: '/aamo/' + data.aamo_id,
    method: 'get'
  })
}

export function saveAapModelo(aapmodelo) {
  const data = {
    aapmodelo
  }
  return request({
    url: '/aamo/save',
    method: 'post',
    data: data.aapmodelo
  })
}

export function updateAapModelo(aapmodelo) {
  const data = {
    aapmodelo
  }
  return request({
    url: '/aamo/upd',
    method: 'post',
    data: data.aapmodelo
  })
}

export function deleteAapModelo(aamo_id) {
  const data = {
    aamo_id
  }
  return request({
    url: '/aamo/del/' + data.aamo_id,
    method: 'get'
  })
}
