import Vue from 'vue'
import Router from 'vue-router'

/* Layout */
import Layout from '../views/layout/Layout'
// in development-env not use lazy-loading, because lazy-loading too many pages will cause webpack hot update too slow. so only in production use lazy-loading;
// detail: https://panjiachen.github.io/vue-element-admin-site/#/lazy-loading

/* import inventarioRouter from './modules/inventario'
import procesoRouter from './modules/proceso'
import actaObraRouter from './modules/acta-obra'
import informeRouter from './modules/informe'
import solicitudRouter from './modules/solicitud'
import auditorRouter from './modules/auditor' */

/* import pqrsRole from './roles/pqrs'
import interventoriaRole from './roles/interventoria'
import facturacionRole from './roles/facturacion'
import inventarioRole from './roles/inventario'
import auditoriaRole from './roles/auditoria'
import gerenciaRole from './roles/gerencia'
import supervisorRole from './roles/supervisor' */

import administradorGroup from './group/administracion'
import operadorGroup from './group/operador'

const _import = require('./_import_' + process.env.NODE_ENV)

Vue.use(Router)

/** note: submenu only apppear when children.length>=1
*   detail see  https://panjiachen.github.io/vue-element-admin-site/#/router-and-nav?id=sidebar
**/

/**
* hidden: true                   if `hidden:true` will not show in the sidebar(default is false)
* alwaysShow: true               if set true, will always show the root menu, whatever its child routes length
*                                if not set alwaysShow, only more than one route under the children
*                                it will becomes nested mode, otherwise not show the root menu
* redirect: noredirect           if `redirect:noredirect` will no redirct in the breadcrumb
* name:'router-name'             the name is used by <keep-alive> (must set!!!)
* meta : {
    roles: ['super', 'admin','editor']     will control the page roles (you can set multiple roles)
    title: 'title'               the name show in submenu and breadcrumb (recommend set)
    icon: 'svg-name'             the icon show in the sidebar,
    noCache: true                if true ,the page will no be cached(default is false)
  }
**/
export const constantRouterMap = [
  { path: '/login', component: _import('login/index'), hidden: true },
  { path: '/recovery', component: _import('recovery/index'), hidden: true },
  { path: '/r/:e', component: _import('recovery/changepass'), hidden: true },
  { path: '/empresa', component: _import('empresa/index'), hidden: true },
  { path: '/authredirect', component: _import('login/authredirect'), hidden: true },
  { path: '/404', component: _import('errorPage/404'), hidden: true },
  { path: '/401', component: _import('errorPage/401'), hidden: true },
  {
    path: '',
    component: Layout,
    redirect: 'dashboard',
    single: true,
    hidden: true,
    meta: { title: 'dashboard', icon: 'dashboard', noCache: true },
    children: [{
      path: 'dashboard',
      component: _import('dashboard/index'),
      name: 'dashboard',
      meta: { title: 'dashboard', icon: 'dashboard', noCache: true }
    }]
  }
]

export default new Router({
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRouterMap
})

export const asyncRouterMap = [
  operadorGroup,
  administradorGroup,
  { path: '*', redirect: '/404', hidden: true }
]
