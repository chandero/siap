import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/time/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getTiposMedidor() {
  return request({
    url: '/time/all',
    method: 'get'
  })
}

export function getTipoMedidor(time_id) {
  const data = {
    time_id
  }
  return request({
    url: '/time/' + data.time_id,
    method: 'get'
  })
}

export function saveTipoMedidor(tipomedidor) {
  const data = {
    tipomedidor
  }
  return request({
    url: '/time/save',
    method: 'post',
    data: data.tipomedidor
  })
}

export function updateTipoMedidor(tipomedidor) {
  const data = {
    tipomedidor
  }
  return request({
    url: '/time/upd',
    method: 'post',
    data: data.tipomedidor
  })
}

export function deleteTipoMedidor(time_id) {
  const data = {
    time_id
  }
  return request({
    url: '/time/del/' + data.time_id,
    method: 'get'
  })
}
