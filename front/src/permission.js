import router from './router'
import store from './store'
import { Message } from 'element-ui'
import NProgress from 'nprogress' // progress bar
import 'nprogress/nprogress.css'// progress bar style
import { getToken, getEmpresaToken } from '@/utils/auth' // getToken from cookie

NProgress.configure({ showSpinner: false })// NProgress Configuration

// permissiom judge function

function hasPermission(roles, permissionRoles) {
  if (roles.indexOf('super') >= 0) return true // super permission passed directly
  if (!permissionRoles) return true
  return roles.some(role => permissionRoles.indexOf(role) >= 0)
}

const whiteList = ['/login', '/authredirect', '/recovery', '/r', '/404']// no redirect whitelist

router.beforeEach((to, from, next) => {
  NProgress.start() // start progress bar
  if (getEmpresaToken()) { // determine if there has token
    /* has empresa token*/
    if (to.path === '/login') {
      next({ path: '/dashboard' })
      NProgress.done() // if current page is dashboard will not trigger	afterEach hook, so manually handle it
    } else {
      if (store.getters.roles.length === 0) {
        store.dispatch('GetUserInfo').then(res => {
          const roles = new Array(res.data.perfil)
          store.dispatch('GenerateRoutes', { roles }).then(() => {
            router.addRoutes(store.getters.addRouters)
            next({ ...to, replace: true })
          })
        }).catch(() => {
          store.dispatch('FedLogOut').then(() => {
            Message.error('Verification failed, please login again')
            next({ path: '/login' })
          })
        })
      } else {
        var routes = new Array(store.getters.roles)
        if (hasPermission(routes, to.meta.roles)) {
          next()
        } else {
          next({ path: '/401', replace: true, query: { noGoBack: true }})
        }
      }
    }
  } else if (getToken()) {
    /* has user token */
    if (to.path === '/login') {
      next({ path: '/empresa' })
      NProgress.done()
    } else if (to.path === '/empresa') {
      next()
      NProgress.done()
    } else {
      next({ path: '/login' })
      NProgress.done()
    }
  } else {
    /* has no token*/
    if (whiteList.indexOf(to.path) !== -1 || to.path.includes('/r/')) {
      next()
    } else {
      next('/login')
      NProgress.done() // if current page is login will not trigger afterEach hook, so manually handle it
    }
  }
})

router.afterEach(() => {
  NProgress.done() // finish progress bar
})
