import Modulo from '@/views/grupo/role/modulo'

const cobranza = {
  path: '/cobranza',
  component: Modulo,
  redirect: 'noredirect',
  single: false,
  name: 'cobranza',
  meta: {
    title: 'Cobranza',
    icon: 'el-icon-search',
    roles: ['super', 'admin', 'supervisor', 'fact']
  },
  children: [
    {
      path: 'menu4orden',
      component: () => import('@/views/proceso/menu4orden/index'),
      name: 'menu4-3cobro',
      meta: { title: 'Opciones', icon: 'el-icon-search', roles: ['super', 'admin', 'supervisor', 'fact'] },
      children: [
        {
          path: 'menu4-3-1list',
          component: () => import('@/views/proceso/menu4orden/menu4-3cobro/menu4-3-1list/index'),
          name: 'menu4-3-1list',
          meta: { title: 'Lista', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
        },
        {
          path: 'menu4-3-2factura',
          component: () => import('@/views/proceso/menu4orden/menu4-3cobro/menu4-3-2factura/index'),
          name: 'menu4-3-2factura',
          meta: { title: 'Factura', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
        },
        {
          path: 'menu4-3-3acta',
          component: () => import('@/views/proceso/menu4orden/menu4-3cobro/menu4-3-3acta/index'),
          name: 'menu4-3-3acta',
          meta: { title: 'Acta', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
        }
      ]
    }
  ]
}

export default cobranza
