/** When your routing table is too long, you can split it into small modules**/

import Layout from '@/views/layout/Layout'

const procesoRouter = {
  path: '/proceso',
  component: Layout,
  redirect: 'noredirect',
  single: false,
  name: 'menu_proceso',
  meta: {
    title: 'menu_proceso',
    icon: 'el-icon-tickets',
    roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor']
  },
  children: [
    {
      path: 'menu1reporte',
      component: () => import('@/views/proceso/menu1reporte/index'), // Parent router-view
      name: 'menu_proceso_menu1reporte',
      meta: { title: 'menu_proceso_menu1reporte', icon: 'el-icon-s-order', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
      redirect: '/proceso/menu1reporte/menu1-1luminaria/menu1-1-1list',
      children: [
        {
          path: 'menu1-1luminaria',
          component: () => import('@/views/proceso/menu1reporte/menu1-1luminaria'),
          name: 'menu_proceso_menu1reporte_menu1-1luminaria',
          meta: { title: 'menu_proceso_menu1reporte_menu1-1luminaria', icon: 'el-icon-grape', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
          redirect: '/proceso/menu1reporte/menu1-1luminaria/menu1-1-1list',
          children: [
            {
              path: 'menu1-1-1list',
              component: () => import('@/views/proceso/menu1reporte/menu1-1luminaria/menu1-1-1list'),
              name: 'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-1list',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-1list', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            },
            {
              path: 'menu1-1-2create/:tireuc_id',
              component: () => import('@/views/proceso/menu1reporte/menu1-1luminaria/menu1-1-2create'),
              name: 'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-2create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-2create', roles: ['super', 'auxiliar', 'supervisor'] }
            },
            {
              path: 'menu1-1-3edit/:id',
              component: () => import('@/views/proceso/menu1reporte/menu1-1luminaria/menu1-1-3edit'),
              name: 'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-3edit',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-3edit', roles: ['super', 'auxiliar', 'supervisor'] }
            }
          ]
        },
        {
          path: 'menu1-2control',
          component: () => import('@/views/proceso/menu1reporte/menu1-2control'),
          name: 'menu_proceso_menu1reporte_menu1-2control',
          meta: { title: 'menu_proceso_menu1reporte_menu1-2control', icon: 'el-icon-watermelon', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
          redirect: '/proceso/menu1reporte/menu1-2control/menu1-2-1list',
          children: [
            {
              path: 'menu1-2-1list',
              component: () => import('@/views/proceso/menu1reporte/menu1-2control/menu1-2-1list'),
              name: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-1list',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-1list', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            },
            {
              path: 'menu1-2-2create/:tireuc_id',
              component: () => import('@/views/proceso/menu1reporte/menu1-2control/menu1-2-2create'),
              name: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-2create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-2create', roles: ['super', 'auxiliar', 'supervisor'] }
            },
            {
              path: 'menu1-2-3edit/:id',
              component: () => import('@/views/proceso/menu1reporte/menu1-2control/menu1-2-3edit'),
              name: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-3create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-3edit', roles: ['super', 'auxiliar', 'supervisor'] }
            }
          ]
        },
        {
          path: 'menu1-3canalizacion',
          component: () => import('@/views/proceso/menu1reporte/menu1-3canalizacion'),
          name: 'menu_proceso_menu1reporte_menu1-3canalizacion',
          meta: { title: 'menu_proceso_menu1reporte_menu1-3canalizacion', icon: 'el-icon-cherry', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
          redirect: '/proceso/menu1reporte/menu1-3canalizacion/menu1-3-1list',
          children: [
            {
              path: 'menu1-3-1list',
              component: () => import('@/views/proceso/menu1reporte/menu1-3canalizacion/menu1-3-1list'),
              name: 'menu_proceso_menu1reporte_menu1-3canalizacion_menu1-3-1list',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-3canalizacion_menu1-3-1list', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            },
            {
              path: 'menu1-3-2create',
              component: () => import('@/views/proceso/menu1reporte/menu1-3canalizacion/menu1-3-2create'),
              name: 'menu_proceso_menu1reporte_menu1-3canalizacion_menu1-3-2create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-3canalizacion_menu1-3-2create', roles: ['super', 'auxiliar', 'supervisor'] }
            },
            {
              path: 'menu1-3-3edit/:id',
              component: () => import('@/views/proceso/menu1reporte/menu1-3canalizacion/menu1-3-3edit'),
              name: 'menu_proceso_menu1reporte_menu1-3canalizacion_menu1-3-3create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-3canalizacion_menu1-3-3edit', roles: ['super', 'auxiliar', 'supervisor'] }
            }
          ]
        },
        {
          path: 'menu1-4poste',
          component: () => import('@/views/proceso/menu1reporte/menu1-4poste'),
          name: 'menu_proceso_menu1reporte_menu1-4poste',
          meta: { title: 'menu_proceso_menu1reporte_menu1-4poste', icon: 'el-icon-wind-power', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
          redirect: '/proceso/menu1reporte/menu1-4poste/menu1-4-1list',
          children: [
            {
              path: 'menu1-4-1list',
              component: () => import('@/views/proceso/menu1reporte/menu1-4poste/menu1-4-1list'),
              name: 'menu_proceso_menu1reporte_menu1-4poste_menu1-4-1list',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-4poste_menu1-4-1list', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            },
            {
              path: 'menu1-4-2create',
              component: () => import('@/views/proceso/menu1reporte/menu1-4poste/menu1-4-2create'),
              name: 'menu_proceso_menu1reporte_menu1-3canalizacion_menu1-4-2create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-4poste_menu1-4-2create', roles: ['super', 'auxiliar', 'supervisor'] }
            },
            {
              path: 'menu1-4-3edit/:id',
              component: () => import('@/views/proceso/menu1reporte/menu1-4poste/menu1-4-3edit'),
              name: 'menu_proceso_menu1reporte_menu1-4poste_menu1-4-3create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-4poste_menu1-4-3edit', roles: ['super', 'auxiliar', 'supervisor'] }
            }
          ]
        },
        {
          path: 'menu1-5redes',
          component: () => import('@/views/proceso/menu1reporte/menu1-5redes'),
          name: 'menu_proceso_menu1reporte_menu1-5redes',
          meta: { title: 'menu_proceso_menu1reporte_menu1-5redes', icon: 'el-icon-phone-outline', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
          redirect: '/proceso/menu1reporte/menu1-5redes/menu1-5-1list',
          children: [
            {
              path: 'menu1-5-1list',
              component: () => import('@/views/proceso/menu1reporte/menu1-5redes/menu1-5-1list'),
              name: 'menu_proceso_menu1reporte_menu1-5redes_menu1-5-1list',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-5redes_menu1-5-1list', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            },
            {
              path: 'menu1-5-2create',
              component: () => import('@/views/proceso/menu1reporte/menu1-5redes/menu1-5-2create'),
              name: 'menu_proceso_menu1reporte_menu1-5redes_menu1-5-2create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-5redes_menu1-5-2create', roles: ['super', 'auxiliar', 'supervisor'] }
            },
            {
              path: 'menu1-5-3edit/:id',
              component: () => import('@/views/proceso/menu1reporte/menu1-5redes/menu1-5-3edit'),
              name: 'menu_proceso_menu1reporte_menu1-5redes_menu1-5-3create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-5redes_menu1-5-3edit', roles: ['super', 'auxiliar', 'supervisor'] }
            }
          ]
        },
        {
          path: 'menu1-6transformador',
          component: () => import('@/views/proceso/menu1reporte/menu1-6transformador'),
          name: 'menu_proceso_menu1reporte_menu1-6transformador',
          meta: { title: 'menu_proceso_menu1reporte_menu1-6transformador', icon: 'el-icon-phone-outline', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
          redirect: '/proceso/menu1reporte/menu1-6transformador/menu1-6-1list',
          children: [
            {
              path: 'menu1-6-1list',
              component: () => import('@/views/proceso/menu1reporte/menu1-6transformador/menu1-6-1list'),
              name: 'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-1list',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-1list', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            },
            {
              path: 'menu1-6-2create',
              component: () => import('@/views/proceso/menu1reporte/menu1-6transformador/menu1-6-2create'),
              name: 'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-2create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-2create', roles: ['super', 'auxiliar', 'supervisor'] }
            },
            {
              path: 'menu1-6-3edit/:id',
              component: () => import('@/views/proceso/menu1reporte/menu1-6transformador/menu1-6-3edit'),
              name: 'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-3create',
              hidden: true,
              meta: { title: 'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-3edit', roles: ['super', 'auxiliar', 'supervisor'] }
            }
          ]
        }
      ]
    },
    {
      path: 'menu2pendiente',
      component: () => import('@/views/proceso/menu2pendiente/index'), // Parent router-view
      name: 'menu_proceso_menu2pendiente',
      meta: { title: 'menu_proceso_menu2pendiente', icon: 'el-icon-s-unfold', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
    },
    {
      path: 'menu5formato',
      component: () => import('@/views/proceso/menu5formato/index'), // Parent router-view
      name: 'menu_proceso_menu5formato',
      meta: { title: 'menu_proceso_menu5formato', icon: 'el-icon-edit-outline', roles: ['super', 'admin', 'auxiliar', 'supervisor'] }
    },
    {
      path: 'menu3obra',
      component: () => import('@/views/proceso/menu3obra/index'), // Parent router-view
      name: 'menu_proceso_menu3obra',
      meta: { title: 'menu_proceso_menu3obra', icon: 'el-icon-picture-outline-round', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
      children: [
        {
          path: 'menu3-1list',
          component: () => import('@/views/proceso/menu3obra/menu3-1list'),
          name: 'menu_proceso_menu3obra_menu3-1list',
          meta: { title: 'menu_proceso_menu3obra_menu3-1list', icon: 'el-icon-s-order', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu3-2create',
          component: () => import('@/views/proceso/menu3obra/menu3-2create'),
          name: 'menu_proceso_menu3obra_menu3-2create',
          hidden: true,
          meta: { title: 'menu_proceso_menu3obra_menu3-2create', roles: ['super', 'auxiliar', 'supervisor'] }
        },
        {
          path: 'menu3-3edit/:id',
          component: () => import('@/views/proceso/menu3obra/menu3-3edit'),
          name: 'menu_proceso_menu3obra_menu3-3edit',
          hidden: true,
          meta: { title: 'menu_proceso_menu3obra_menu3-3edit', roles: ['super', 'auxiliar', 'supervisor'] }
        },
        {
          path: 'menu3-4pendiente',
          component: () => import('@/views/proceso/menu3obra/menu3-4pendiente'),
          name: 'menu_proceso_menu3obra_menu3-4pendiente',
          meta: { title: 'menu_proceso_menu3obra_menu3-4pendiente', icon: 'el-icon-s-unfold', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
        }
      ]
    },
    {
      path: 'menu4orden',
      component: () => import('@/views/proceso/menu4orden/index'), // Parent router-view
      name: 'menu_proceso_menu4orden',
      meta: { title: 'menu_proceso_menu4orden', icon: 'el-icon-document-add', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
      redirect: '/proceso/menu4orden/menu4-1interna/menu4-1-1list',
      children: [
        {
          path: 'menu4-1interna',
          component: () => import('@/views/proceso/menu4orden/menu4-1interna'),
          name: 'menu_proceso_menu4orden_menu4-1interna',
          meta: { title: 'menu_proceso_menu4orden_menu4-1interna', icon: 'el-icon-truck', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
          redirect: '/proceso/menu4orden/menu4-1interna/menu4-1-1list',
          children: [
            {
              path: 'menu4-1-1list',
              component: () => import('@/views/proceso/menu4orden/menu4-1interna/menu4-1-1list'),
              name: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-1list',
              hidden: true,
              meta: { title: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-1list', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            },
            {
              path: 'menu4-1-2create',
              component: () => import('@/views/proceso/menu4orden/menu4-1interna/menu4-1-2create'),
              name: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-2create',
              hidden: true,
              meta: { title: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-2create', roles: ['super', 'admin', 'auxiliar', 'supervisor'] }
            },
            {
              path: 'menu4-1-3edit/:id',
              component: () => import('@/views/proceso/menu4orden/menu4-1interna/menu4-1-3edit'),
              name: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-3edit',
              hidden: true,
              meta: { title: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-3edit', roles: ['super', 'admin', 'auxiliar', 'supervisor'] }
            }
          ]
        },
        {
          path: 'menu4-2externa',
          component: () => import('@/views/proceso/menu4orden/menu4-2externa'),
          name: 'menu_proceso_menu4orden_menu4-2externa',
          hidden: true,
          meta: { title: 'menu_proceso_menu4orden_menu4-2externa', icon: 'el-icon-receiving', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
          redirect: '/proceso/menu4orden/menu4-2externa/menu4-2-1list',
          children: [
            {
              path: 'menu4-2-1list',
              component: () => import('@/views/proceso/menu4orden/menu4-2externa/menu4-2-1list'),
              name: 'menu_proceso_menu4orden_menu4-1externa_menu4-2-1list',
              hidden: true,
              meta: { title: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-1list', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            },
            {
              path: 'menu4-2-2create',
              component: () => import('@/views/proceso/menu4orden/menu4-2externa/menu4-2-2create'),
              name: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-2create',
              hidden: true,
              meta: { title: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-2create', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            },
            {
              path: 'menu4-2-3edit/:id',
              component: () => import('@/views/proceso/menu4orden/menu4-2externa/menu4-2-3edit'),
              name: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-3edit',
              hidden: true,
              meta: { title: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-3edit', roles: ['super', 'auxiliar', 'supervisor'] }
            },
            {
              path: 'menu4-2-4info/:id',
              component: () => import('@/views/proceso/menu4orden/menu4-2externa/menu4-2-4info'),
              name: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-4info',
              hidden: true,
              meta: { title: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-4info', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] }
            }
          ]
        },
        {
          path: 'menu4-3cobro',
          component: () => import('@/views/proceso/menu4orden/menu4-3cobro'),
          name: 'menu_proceso_menu4orden_menu4-3cobro',
          meta: { title: 'menu_proceso_menu4orden_menu4-3cobro', icon: 'el-icon-receiving', roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor'] },
          redirect: '/proceso/menu4orden/menu4-3cobro/menu4-3-1list',
          children: [
            {
              path: 'menu4-3-1list',
              component: () => import('@/views/proceso/menu4orden/menu4-3cobro/menu4-3-1list'),
              name: 'menu_proceso_menu4orden_menu4-3cobro_menu4-3-1list',
              hidden: true,
              meta: { title: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-1list', roles: ['super', 'admin', 'auxiliar', 'supervisor'] }
            }
          ]
        }
      ]
    },
    {
      path: 'menu6recuperar',
      component: () => import('@/views/proceso/menu6recuperar/index'), // Parent router-view
      name: 'menu_proceso_menu6recuperar',
      meta: { title: 'menu_proceso_menu6recuperar', icon: 'el-icon-refresh-right', roles: ['super', 'auxiliar'] }
    }
  ]
}

export default procesoRouter
