import axios from 'axios'
import { Message } from 'element-ui'

// create an axios instance
const service = axios.create({
  baseURL: process.env.BASE_API, // api的base_url
  timeout: 5000, // request timeout
  headers: {
    'Content-Type': 'application/pdf'
  }
})

// request interceptor
service.interceptors.request.use(config => {
  // Do something before request is sent

  if (localStorage.token) {
    config.headers.Authorization = localStorage.token
  }
  config.headers['Access-Control-Allow-Origin'] = '*'
  config.headers['Access-Control-Allow-Credentials'] = 'true'
  return config
}, error => {
  // Do something with request error
  console.log(error) // for debug
  Promise.reject(error)
})

// respone interceptor
service.interceptors.response.use(
  response => response,
  error => {
    console.log('err' + error)// for debug
    Message({
      message: error.message,
      type: 'error',
      duration: 5 * 1000
    })
    return Promise.reject(error)
  })

export default service
