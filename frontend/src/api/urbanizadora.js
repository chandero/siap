import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/urba/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getUrbanizadoraTodas () {
  return request({
    url: '/urba/all',
    method: 'get'
  })
}

export function getUrbanizadora (urba_id) {
  const data = {
    urba_id
  }
  return request({
    url: '/urba/' + data.urba_id,
    method: 'get'
  })
}

export function saveUrbanizadora (urba) {
  const data = {
    urba
  }
  return request({
    url: '/urba/save',
    method: 'post',
    data: data.urba
  })
}

export function updateUrbanizadora (urba) {
  const data = {
    urba
  }
  return request({
    url: '/urba/upd',
    method: 'post',
    data: data.urba
  })
}

export function deleteUrbanizadora (urba_id) {
  const data = {
    urba_id
  }
  return request({
    url: '/urba/del/' + data.urba_id,
    method: 'get'
  })
}
