import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/aama/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getAapMarcas() {
  return request({
    url: '/aama/all',
    method: 'get'
  })
}

export function getAapMarca(aama_id) {
  const data = {
    aama_id
  }
  return request({
    url: '/aama/' + data.aama_id,
    method: 'get'
  })
}

export function saveAapMarca(aapmarca) {
  const data = {
    aapmarca
  }
  return request({
    url: '/aama/save',
    method: 'post',
    data: data.aapmarca
  })
}

export function updateAapMarca(aapmarca) {
  const data = {
    aapmarca
  }
  return request({
    url: '/aama/upd',
    method: 'post',
    data: data.aapmarca
  })
}

export function deleteAapMarca(aama_id) {
  const data = {
    aama_id
  }
  return request({
    url: '/aama/del/' + data.aama_id,
    method: 'get'
  })
}
