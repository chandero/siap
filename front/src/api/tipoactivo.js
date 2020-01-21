import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/tiac/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getTiposActivo() {
  return request({
    url: '/tiac/all',
    method: 'get'
  })
}

export function getTipoActivo(tiac_id) {
  const data = {
    tiac_id
  }
  return request({
    url: '/tiac/' + data.tiac_id,
    method: 'get'
  })
}

export function saveTipoActivo(tire) {
  const data = {
    tire
  }
  return request({
    url: '/tiac/save',
    method: 'post',
    data: data.tire
  })
}

export function updateTipoActivo(tire) {
  const data = {
    tire
  }
  return request({
    url: '/tiac/upd',
    method: 'post',
    data: data.tire
  })
}

export function deleteTipoActivo(tiac_id) {
  const data = {
    tiac_id
  }
  return request({
    url: '/tiac/del/' + data.tiac_id,
    method: 'get'
  })
}
