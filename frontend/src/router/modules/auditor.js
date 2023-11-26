import Modulo from '@/views/grupo/role/modulo'

const auditorRouter = {
  path: '/auditor',
  component: Modulo,
  redirect: 'noredirect',
  single: false,
  name: 'menu_auditor',
  meta: {
    title: 'menu_auditor',
    icon: 'el-icon-search',
    roles: ['super', 'admin', 'supervisor']
  },
  children: [
    {
      path: 'menu1codigoautorizacion',
      component: () => import('@/views/auditor/menu1codigoautorizacion'),
      name: 'menu_auditor_menu1codigoautorizacion',
      meta: { title: 'menu_auditor_menu1codigoautorizacion', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor'] }
    }
  ]
}

export default auditorRouter
