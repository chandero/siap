import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/nove/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getNovedades () {
  return request({
    url: '/nove/all',
    method: 'get'
  })
}

export function getNovedad (nove_id) {
  const data = {
    nove_id
  }
  return request({
    url: '/nove/' + data.nove_id,
    method: 'get'
  })
}

export function saveNovedad (nove) {
  const data = {
    nove
  }
  return request({
    url: '/nove/save',
    method: 'post',
    data: data.nove
  })
}

export function updateNovedad (nove) {
  const data = {
    nove
  }
  return request({
    url: '/nove/upd',
    method: 'post',
    data: data.nove
  })
}

export function deleteNovedad (nove_id) {
  const data = {
    nove_id
  }
  return request({
    url: '/nove/del/' + data.nove_id,
    method: 'get'
  })
}
