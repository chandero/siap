import request from '@/utils/request'

export function getTodos (page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/aatc/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getAapTiposCarcasa () {
  return request({
    url: '/aatc/all',
    method: 'get'
  })
}

export function getAapTipoCarcasa (aatc_id) {
  const data = {
    aatc_id
  }
  return request({
    url: '/aatc/' + data.aatc_id,
    method: 'get'
  })
}

export function saveAapTipoCarcasa (aaptipocarcasa) {
  const data = {
    aaptipocarcasa
  }
  return request({
    url: '/aatc/save',
    method: 'post',
    data: data.aaptipocarcasa
  })
}

export function updateAapTipoCarcasa (aaptipocarcasa) {
  const data = {
    aaptipocarcasa
  }
  return request({
    url: '/aatc/upd',
    method: 'post',
    data: data.aaptipocarcasa
  })
}

export function deleteAapTipoCarcasa (aatc_id) {
  const data = {
    aatc_id
  }
  return request({
    url: '/aatc/del/' + data.aatc_id,
    method: 'get'
  })
}
