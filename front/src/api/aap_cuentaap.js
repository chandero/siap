import request from '@/utils/request'

export function getTodos(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/aacu/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getAapCuentasAp() {
  return request({
    url: '/aacu/all',
    method: 'get'
  })
}

export function getAapCuentaAp(aacu_id) {
  const data = {
    aacu_id
  }
  return request({
    url: '/aacu/' + data.aacu_id,
    method: 'get'
  })
}

export function saveAapCuentaAp(aapcuentaap) {
  const data = {
    aapcuentaap
  }
  return request({
    url: '/aacu/save',
    method: 'post',
    data: data.aapcuentaap
  })
}

export function updateAapCuentaAp(aapcuentaap) {
  const data = {
    aapcuentaap
  }
  return request({
    url: '/aacu/upd',
    method: 'post',
    data: data.aapcuentaap
  })
}

export function deleteAapCuentaAp(aacu_id) {
  const data = {
    aacu_id
  }
  return request({
    url: '/aacu/del/' + data.aacu_id,
    method: 'get'
  })
}
