import request from '@/utils/request'

export function getPerfiles(page_size, current_page) {
  const data = {
    page_size,
    current_page
  }
  return request({
    url: '/perf/get/' + data.page_size + '/' + data.current_page,
    method: 'get'
  })
}

export function getPerfil(id) {
  const data = {
    id
  }
  return request({
    url: '/perf/gbi/' + data.id,
    method: 'get'
  })
}

export function getUsuarioPerfil(id) {
  const data = {
    id
  }
  return request({
    url: '/perf/gbui/' + data.id,
    method: 'get'
  })
}
