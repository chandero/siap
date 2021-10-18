export function isReachable () {
  return new Promise(resolve => {
    /*
    url = new URL(data.url)
    const hostname = url.hostname
    const protocol = url.protocol || ''
    const port = url.port ? `:${url.port}` : ''
    */
    const img = new Image()
    img.onload = () => resolve(true)
    img.onerror = () => resolve(false)
    img.src = '/favicon.ico'
  })
}
