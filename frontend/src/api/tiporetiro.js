import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/tire/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getTiposRetiro () {
  return request({
    url: '/tire/all',
    method: 'get'
  })
}

export function getTipoRetiro (tire_id) {
  const data = {
    tire_id
  }
  return request({
    url: '/tire/' + data.tire_id,
    method: 'get'
  })
}

export function saveTipoRetiro (tire) {
  const data = {
    tire
  }
  return request({
    url: '/tire/save',
    method: 'post',
    data: data.tire
  })
}

export function updateTipoRetiro (tire) {
  const data = {
    tire
  }
  return request({
    url: '/tire/upd',
    method: 'post',
    data: data.tire
  })
}

export function deleteTipoRetiro (tire_id) {
  const data = {
    tire_id
  }
  return request({
    url: '/tire/del/' + data.tire_id,
    method: 'get'
  })
}
