/** When your routing table is too long, you can split it into small modules**/

import Layout from '@/views/layout/Layout'

const procesoRouter = {
  path: '/proceso',
  component: Layout,
  redirect: '/proceso/menu1reporte/list',
  name: 'menu_proceso',
  meta: {
    title: 'menu_proceso',
    icon: 'el-icon-tickets',
    roles: ['super', 'admin', 'operador', 'supervisor']
  },
  children: [
    {
      path: 'menu1reporte',
      component: () => import('@/views/proceso/menu1reporte/index'), // Parent router-view
      name: 'menu_proceso_menu1reporte',
      meta: { title: 'menu_proceso_menu1reporte', icon: 'el-icon-s-order', roles: ['super', 'admin', 'operador', 'supervisor'] },
      redirect: '/proceso/menu1reporte/menu1-1luminaria',
      children: [
        {
          path: 'menu1-1luminaria',
          component: () => import('@/views/proceso/menu1reporte/menu1-1luminaria'),
          name: 'menu_proceso_menu1reporte_menu1-1luminaria',
          meta: { title: 'menu_proceso_menu1reporte_menu1-1luminaria', icon: 'el-icon-grape', roles: ['super', 'admin', 'operador', 'supervisor'] }
        },
        {
          path: 'menu1-2create',
          component: () => import('@/views/proceso/menu1reporte/menu1-2create'),
          name: 'menu_proceso_menu1reporte_menu1-2create',
          hidden: true,
          meta: { title: 'menu_proceso_menu1reporte_menu1-2create', roles: ['super', 'admin', 'operador', 'supervisor'] }
        },
        {
          path: 'menu1-3edit/:id',
          component: () => import('@/views/proceso/menu1reporte/menu1-3edit'),
          name: 'menu_proceso_menu1reporte_menu1-3create',
          hidden: true,
          meta: { title: 'menu_proceso_menu1reporte_menu1-3edit', roles: ['super', 'admin', 'operador', 'supervisor'] }
        }
      ]
    }
  ]
}

export default procesoRouter
