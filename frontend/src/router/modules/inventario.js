/** When your routing table is too long, you can split it into small modules**/

import Modulo from '@/views/grupo/role/modulo'

const inventarioRouter = {
  path: '/inven',
  component: Modulo,
  redirect: 'noredirect',
  name: 'menu_inventario',
  meta: {
    title: 'menu_inventario',
    icon: 'el-icon-files',
    roles: ['super', 'admin', 'pqrs', 'ingeniero', 'gerencia', 'supervisor']
  },
  children: [
    {
      path: 'menu1luminaria',
      component: () => import('@/views/inventario/menu1luminaria/index'), // Parent router-view
      name: 'menu_inventario_menu1luminaria',
      meta: { title: 'menu_inventario_menu1luminaria', icon: 'el-icon-grape', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] },
      redirect: '/inventario/menu1inventario/menu1-1activa',
      children: [
        {
          path: 'menu1-1activa',
          component: () => import('@/views/inventario/menu1luminaria/menu1-1activa'),
          name: 'menu_inventario_menu1luminaria_menu1-1activa',
          meta: { title: 'menu_inventario_menu1luminaria_menu1-1activa', icon: 'el-icon-sunrise', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        { path: 'menu1-1activa/gestion/editar/:id', component: () => import('@/views/inventario/menu1luminaria/menu1-1activa/gestion/edit/index'), name: 'gestionedit', hidden: true, redirect: false, meta: { title: 'gestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } },
        { path: 'menu1-1activa/gestion/crear', component: () => import('@/views/inventario/menu1luminaria/menu1-1activa/gestion/create/index'), name: 'gestioncreate', hidden: true, meta: { title: 'gestioncreate', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } },
        {
          path: 'menu1-2enbaja',
          component: () => import('@/views/inventario/menu1luminaria/menu1-2enbaja'),
          name: 'menu_inventario_menu1luminaria_menu1-2enbaja',
          meta: { title: 'menu_inventario_menu1luminaria_menu1-2enbaja', icon: 'el-icon-sunset', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu1-3porcodigo',
          component: () => import('@/views/inventario/menu1luminaria/menu1-3porcodigo'),
          name: 'menu_inventario_menu1luminaria_menu1-3porcodigo',
          meta: { title: 'menu_inventario_menu1luminaria_menu1-3porcodigo', icon: 'el-icon-search', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu1-6pormaterial',
          component: () => import('@/views/inventario/menu1luminaria/menu1-6pormaterial'),
          name: 'menu_inventario_menu1luminaria_menu1-6pormaterial',
          meta: { title: 'menu_inventario_menu1luminaria_menu1-6pormaterial', icon: 'el-icon-search', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu1-4historia',
          component: () => import('@/views/inventario/menu1luminaria/menu1-4historia'),
          name: 'menu_inventario_menu1luminaria_menu1-4historia',
          meta: { title: 'menu_inventario_menu1luminaria_menu1-4historia', icon: 'el-icon-document-copy', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu1-5geo',
          component: () => import('@/views/inventario/menu1luminaria/menu1-5geo'),
          name: 'menu_inventario_menu1luminaria_menu1-5geo',
          meta: { title: 'menu_inventario_menu1luminaria_menu1-5geo', icon: 'el-icon-location-information', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        }
      ]
    },
    {
      path: 'menu2control',
      component: () => import('@/views/inventario/menu2control/index'), // Parent router-view
      name: 'menu_inventario_menu2control',
      meta: { title: 'menu_inventario_menu2control', icon: 'el-icon-watermelon', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] },
      redirect: '/inventario/menu2control/menu2-1activa',
      children: [
        {
          path: 'menu2-1activa',
          component: () => import('@/views/inventario/menu2control/menu2-1activa'),
          name: 'menu_inventario_menu2control_menu2-1activa',
          meta: { title: 'menu_inventario_menu2control_menu2-1activa', icon: 'el-icon-sunrise', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu2-2enbaja',
          component: () => import('@/views/inventario/menu2control/menu2-2enbaja'),
          name: 'menu_inventario_menu2control_menu2-2enbaja',
          meta: { title: 'menu_inventario_menu2control_menu2-2enbaja', icon: 'el-icon-sunset', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        { path: 'menu2-1activa/gestion/editar/:id', component: () => import('@/views/inventario/menu2control/menu2-1activa/gestion/edit/index'), name: 'controlgestionedit', hidden: true, redirect: false, meta: { title: 'controlgestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } },
        { path: 'menu2-1activa/gestion/crear', component: () => import('@/views/inventario/menu2control/menu2-1activa/gestion/create/index'), name: 'controlgestioncreate', hidden: true, meta: { title: 'controlgestioncreate', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } }
      ]
    },
    {
      path: 'menu3canalizacion',
      component: () => import('@/views/inventario/menu3canalizacion/index'), // Parent router-view
      name: 'menu_inventario_menu3canalizacion',
      meta: { title: 'menu_inventario_menu3canalizacion', icon: 'el-icon-cherry', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] },
      redirect: '/inventario/menu3canalizacion/menu3-1activa',
      children: [
        {
          path: 'menu3-1activa',
          component: () => import('@/views/inventario/menu3canalizacion/menu3-1activa'),
          name: 'menu_inventario_menu3canalizacion_menu3-1activa',
          meta: { title: 'menu_inventario_menu3canalizacion_menu3-1activa', icon: 'el-icon-sunrise', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu3-2enbaja',
          component: () => import('@/views/inventario/menu3canalizacion/menu3-2enbaja'),
          name: 'menu_inventario_menu3canalizacion_menu3-2enbaja',
          meta: { title: 'menu_inventario_menu3canalizacion_menu3-2enbaja', icon: 'el-icon-sunset', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        { path: 'menu3-1activa/gestion/editar/:id', component: () => import('@/views/inventario/menu3canalizacion/menu3-1activa/gestion/edit/index'), name: 'canalizaciongestionedit', hidden: true, redirect: false, meta: { title: 'canalizaciongestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } },
        { path: 'menu3-1activa/gestion/crear', component: () => import('@/views/inventario/menu3canalizacion/menu3-1activa/gestion/create/index'), name: 'canalizaciongestioncreate', hidden: true, meta: { title: 'canalizaciongestioncreate', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } }
      ]
    },
    {
      path: 'menu4poste',
      component: () => import('@/views/inventario/menu4poste/index'), // Parent router-view
      name: 'menu_inventario_menu4poste',
      meta: { title: 'menu_inventario_menu4poste', icon: 'el-icon-wind-power', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] },
      redirect: '/inventario/menu4poste/menu4-1activa',
      children: [
        {
          path: 'menu4-1activa',
          component: () => import('@/views/inventario/menu4poste/menu4-1activa'),
          name: 'menu_inventario_menu4poste_menu4-1activa',
          meta: { title: 'menu_inventario_menu4poste_menu4-1activa', icon: 'el-icon-sunrise', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu4-2enbaja',
          component: () => import('@/views/inventario/menu4poste/menu4-2enbaja'),
          name: 'menu_inventario_menu4poste_menu4-2enbaja',
          meta: { title: 'menu_inventario_menu4poste_menu4-2enbaja', icon: 'el-icon-sunset', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        { path: 'menu4-1activa/gestion/editar/:id', component: () => import('@/views/inventario/menu4poste/menu4-1activa/gestion/edit/index'), name: 'postegestionedit', hidden: true, redirect: false, meta: { title: 'postegestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } },
        { path: 'menu4-1activa/gestion/crear', component: () => import('@/views/inventario/menu4poste/menu4-1activa/gestion/create/index'), name: 'postegestioncreate', hidden: true, meta: { title: 'postegestioncreate', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } }
      ]
    },
    {
      path: 'menu5redes',
      component: () => import('@/views/inventario/menu5redes/index'), // Parent router-view
      name: 'menu_inventario_menu5redes',
      meta: { title: 'menu_inventario_menu5redes', icon: 'el-icon-phone-outline', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] },
      redirect: '/inventario/menu5redes/menu5-1activa',
      children: [
        {
          path: 'menu5-1activa',
          component: () => import('@/views/inventario/menu5redes/menu5-1activa'),
          name: 'menu_inventario_menu5redes_menu5-1activa',
          meta: { title: 'menu_inventario_menu5redes_menu5-1activa', icon: 'el-icon-sunrise', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu5-2enbaja',
          component: () => import('@/views/inventario/menu5redes/menu5-2enbaja'),
          name: 'menu_inventario_menu5redes_menu5-2enbaja',
          meta: { title: 'menu_inventario_menu5redes_menu5-2enbaja', icon: 'el-icon-sunset', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        { path: 'menu5-1activa/gestion/editar/:id', component: () => import('@/views/inventario/menu5redes/menu5-1activa/gestion/edit/index'), name: 'redesgestionedit', hidden: true, redirect: false, meta: { title: 'redesgestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } },
        { path: 'menu5-1activa/gestion/crear', component: () => import('@/views/inventario/menu5redes/menu5-1activa/gestion/create/index'), name: 'redesgestioncreate', hidden: true, meta: { title: 'redesgestioncreate', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } }
      ]
    },
    {
      path: 'menu6transformador',
      component: () => import('@/views/inventario/menu6transformador/index'), // Parent router-view
      name: 'menu_inventario_menu6transformador',
      meta: { title: 'menu_inventario_menu6transformador', icon: 'el-icon-c-scale-to-original', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] },
      redirect: '/inventario/menu6transformador/menu6-1activa',
      children: [
        {
          path: 'menu6-1activa',
          component: () => import('@/views/inventario/menu6transformador/menu6-1activa'),
          name: 'menu_inventario_menu6transformador_menu6-1activa',
          meta: { title: 'menu_inventario_menu6transformador_menu6-1activa', icon: 'el-icon-sunrise', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu6-2enbaja',
          component: () => import('@/views/inventario/menu6transformador/menu6-2enbaja'),
          name: 'menu_inventario_menu6transformador_menu6-2enbaja',
          meta: { title: 'menu_inventario_menu6transformador_menu6-2enbaja', icon: 'el-icon-sunset', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        { path: 'menu6-1activa/gestion/edit/:id', component: () => import('@/views/inventario/menu6transformador/menu6-1activa/gestion/edit/index'), name: 'transformadorgestionedit', hidden: true, redirect: false, meta: { title: 'transformadorgestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } },
        { path: 'menu6-1activa/gestion/create', component: () => import('@/views/inventario/menu6transformador/menu6-1activa/gestion/create/index'), name: 'transformadorgestioncreate', hidden: true, meta: { title: 'transformadorgestioncreate', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } }
      ]
    },
    {
      path: 'menu7medidor',
      component: () => import('@/views/inventario/menu7medidor/index'), // Parent router-view
      name: 'menu_inventario_menu7medidor',
      meta: { title: 'menu_inventario_menu7medidor', icon: 'el-icon-c-scale-to-original', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] },
      redirect: '/inventario/menu7medidor/menu7-1activa',
      children: [
        /* {
          path: 'menu7-1activa',
          component: () => import('@/views/inventario/menu7medidor/menu7-1activa'),
          name: 'menu_inventario_menu7medidor_menu7-1activa',
          meta: { title: 'menu_inventario_menu7medidor_menu7-1activa', icon: 'el-icon-sunrise', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        {
          path: 'menu7-2enbaja',
          component: () => import('@/views/inventario/menu7medidor/menu7-2enbaja'),
          name: 'menu_inventario_menu7medidor_menu7-2enbaja',
          meta: { title: 'menu_inventario_menu7medidor_menu7-2enbaja', icon: 'el-icon-sunset', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        },
        { path: 'menu7-1activa/gestion/edit/:id', component: () => import('@/views/inventario/menu7medidor/menu7-1activa/gestion/edit/index'), name: 'medidorgestionedit', hidden: true, redirect: false, meta: { title: 'medidorgestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } },
        { path: 'menu7-1activa/gestion/create', component: () => import('@/views/inventario/menu7medidor/menu7-1activa/gestion/create/index'), name: 'medidorgestioncreate', hidden: true, meta: { title: 'medidorgestioncreate', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } }, */
        {
          path: 'menu7-3carga',
          component: () => import('@/views/inventario/menu7medidor/menu7-3carga'),
          name: 'menu_inventario_menu7medidor_menu7-3carga',
          meta: { title: 'menu_inventario_menu7medidor_menu7-3carga', icon: 'el-icon-sunrise', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
        }
      ]
    }
  ]
}

export default inventarioRouter
