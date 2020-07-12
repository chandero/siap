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
