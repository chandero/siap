/** When your routing table is too long, you can split it into small modules**/
import Layout from '@/views/layout/Layout'

const solicitudRouter = {
  path: '/solicitud',
  component: Layout,
  redirect: 'noredirect',
  name: 'menu_solicitud',
  meta: {
    title: 'menu_solicitud',
    icon: 'el-icon-postcard',
    roles: ['super', 'admin', 'auxiliar', 'gerencia', 'supervisor']
  },
  children: [
    {
      path: 'menu1carta',
      component: () => import('@/views/solicitud/menu1carta/index'), // Parent router-view
      name: 'menu_solicitud_menu1carta',
      meta: { title: 'menu_solicitud_menu1carta', icon: 'el-icon-message', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'supervisor'] },
      redirect: '/solicitud/menu1carta/menu1-1list',
      children: [
        {
          path: 'menu1-1list',
          component: () => import('@/views/solicitud/menu1carta/menu1-1list'),
          name: 'menu_solicitud_menu1carta_menu1-1list',
          hidden: true,
          meta: { title: 'menu_solicitud_menu1carta_menu1-1list', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'supervisor'] }
        },
        {
          path: 'menu1-2create',
          component: () => import('@/views/solicitud/menu1carta/menu1-2create'),
          name: 'menu_solicitud_menu1carta_menu1-2create',
          hidden: true,
          meta: { title: 'menu_solicitud_menu1carta_menu1-2create', roles: ['super', 'auxiliar', 'supervisor'] }
        },
        {
          path: 'menu1-3view/:id',
          component: () => import('@/views/solicitud/menu1carta/menu1-3view'),
          name: 'menu_solicitud_menu1carta_menu1-3view',
          hidden: true,
          meta: { title: 'menu_solicitud_menu1carta_menu1-3view', roles: ['super', 'auxiliar', 'supervisor'] }
        },
        {
          path: 'menu1-4edit/:id',
          component: () => import('@/views/solicitud/menu1carta/menu1-4edit'),
          name: 'menu_solicitud_menu1carta_menu1-4edit',
          hidden: true,
          meta: { title: 'menu_solicitud_menu1carta_menu1-4edit', roles: ['super', 'auxiliar', 'supervisor'] }
        }
      ]
    },
    {
      path: 'menu2almacen',
      component: () => import('@/views/solicitud/menu2almacen'),
      name: 'menu_solicitud_menu2almacen',
      meta: { title: 'menu_solicitud_menu2almacen', icon: 'el-icon-school', roles: ['super', 'auxiliar', 'supervisor'] }
    },
    {
      path: 'menu3detallado',
      component: () => import('@/views/solicitud/menu3detallado'),
      name: 'menu_solicitud_menu3detallado',
      meta: { title: 'menu_solicitud_menu3detallado', icon: 'el-icon-tickets', roles: ['super', 'admin', 'gerencia', 'auxiliar', 'supervisor'] }
    },
    {
      path: 'menu4porvencer',
      component: () => import('@/views/solicitud/menu4porvencer'),
      name: 'menu_solicitud_menu4porvencer',
      meta: { title: 'menu_solicitud_menu4porvencer', icon: 'el-icon-s-comment', roles: ['super', 'admin', 'gerencia', 'auxiliar', 'supervisor'] }
    }
  ]
}

export default solicitudRouter
