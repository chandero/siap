import Modulo from '@/views/grupo/role/modulo'

const actaObraRouter = {
  path: '/ao',
  component: Modulo,
  redirect: 'noredirect',
  single: false,
  name: 'menu_acta_obra',
  meta: {
    title: 'menu_acta_obra',
    icon: 'el-icon-truck',
    roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor']
  },
  children: [
    {
      path: 'menu1list',
      component: () => import('@/views/acta-obra/menu1list/index'),
      name: 'menu_acta_obra_menu1list',
      meta: {
        title: 'menu_acta_obra_menu1list',
        noCache: true,
        icon: 'el-icon-document-remove',
        roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor']
      }
    },
    {
      path: 'menu2create',
      component: () => import('@/views/acta-obra/menu2create/index'),
      name: 'menu_acta_obra_menu2create',
      hidden: true,
      meta: {
        title: 'menu_acta_obra_menu2create',
        noCache: true,
        icon: 'el-icon-document-remove',
        roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor']
      }
    },
    {
      path: 'menu3edit/:id',
      component: () => import('@/views/acta-obra/menu3edit/index'),
      name: 'menu_acta_obra_menu3edit',
      hidden: true,
      meta: {
        title: 'menu_acta_obra_menu3edit',
        noCache: true,
        icon: 'el-icon-document-remove',
        roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor']
      }
    },
    {
      path: 'menu4item',
      component: () => import('@/views/acta-obra/menu4item/index'),
      name: 'menu_acta_obra_menu4item',
      hidden: false,
      meta: {
        title: 'menu_acta_obra_menu4item',
        noCache: true,
        icon: 'el-icon-document-remove',
        roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor']
      }
    }
  ]
}

export default actaObraRouter
