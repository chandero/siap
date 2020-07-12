import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/aaus/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getAapUsos() {
  return request({
    url: '/aaus/all',
    method: 'get'
  })
}

export function getAapUso(aaus_id) {
  const data = {
    aaus_id
  }
  return request({
    url: '/aaus/' + data.aaus_id,
    method: 'get'
  })
}

export function saveAapUso(aapuso) {
  const data = {
    aapuso
  }
  return request({
    url: '/aaus/save',
    method: 'post',
    data: data.aapuso
  })
}

export function updateAapUso(aapuso) {
  const data = {
    aapuso
  }
  return request({
    url: '/aaus/upd',
    method: 'post',
    data: data.aapuso
  })
}

export function deleteAapUso(aaus_id) {
  const data = {
    aaus_id
  }
  return request({
    url: '/aaus/del/' + data.aaus_id,
    method: 'get'
  })
}
