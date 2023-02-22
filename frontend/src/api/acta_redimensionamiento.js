import request from '@/utils/request'

export function getActaRedimensionamiento(page_size, current_page, orderby, filter) {
  return request({
    url: `/acre`,
    method: 'post',
    data: {
        page_size,
        current_page,
        orderby,
        filter
    }
  })
}