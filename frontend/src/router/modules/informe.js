/** When your routing table is too long, you can split it into small modules**/

import Layout from '@/views/layout/Layout'

const informeRouter = {
  path: '/consolidado',
  component: Layout,
  redirect: 'noredirect',
  single: false,
  name: 'menu_informe',
  meta: {
    title: 'menu_informe',
    icon: 'el-icon-printer',
    roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor']
  },
  children: [
    {
      path: 'menu1consolidado',
      component: () => import('@/views/informe/menu1consolidado/index'), // Parent router-view
      name: 'menu_informe_menu1consolidado',
      meta: { title: 'menu_informe_menu1consolidado', icon: 'el-icon-document-remove', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] },
      redirect: '/informe/menu1consolidado/menu1-1material',
      children: [
        {
          path: 'menu1-1material',
          component: () => import('@/views/informe/menu1consolidado/menu1-1material'),
          name: 'menu_informe_menu1consolidado_menu1-1material',
          meta: { title: 'menu_informe_menu1consolidado_menu1-1material', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu1-2portipo',
          component: () => import('@/views/informe/menu1consolidado/menu1-2portipo'),
          name: 'menu_informe_menu1consolidado_menu1-2portipo',
          meta: { title: 'menu_informe_menu1consolidado_menu1-2portipo', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu1-3reporte',
          component: () => import('@/views/informe/menu1consolidado/menu1-3reporte'),
          name: 'menu_informe_menu1consolidado_menu1-3reporte',
          meta: { title: 'menu_informe_menu1consolidado_menu1-3reporte', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu1-8materialcuadrilla',
          component: () => import('@/views/informe/menu1consolidado/menu1-8materialcuadrilla'),
          name: 'menu_informe_menu1consolidado_menu1-8materialcuadrilla',
          meta: { title: 'menu_informe_menu1consolidado_menu1-8materialcuadrilla', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu1-4cuadrilla',
          component: () => import('@/views/informe/menu1consolidado/menu1-4cuadrilla'),
          name: 'menu_informe_menu1consolidado_menu1-4cuadrilla',
          meta: { title: 'menu_informe_menu1consolidado_menu1-4cuadrilla', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu1-5operaciones',
          component: () => import('@/views/informe/menu1consolidado/menu1-5operaciones'),
          name: 'menu_informe_menu1consolidado_menu1-5operaciones',
          meta: { title: 'menu_informe_menu1consolidado_menu1-5operaciones', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu1-6estadistica',
          component: () => import('@/views/informe/menu1consolidado/menu1-6estadistica'),
          name: 'menu_informe_menu1consolidado_menu1-6estadistica',
          meta: { title: 'menu_informe_menu1consolidado_menu1-6estadistica', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu1-7aforo',
          component: () => import('@/views/informe/menu1consolidado/menu1-7aforo'),
          name: 'menu_informe_menu1consolidado_menu1-7aforo',
          meta: { title: 'menu_informe_menu1consolidado_menu1-7aforo', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        }
      ]
    },
    {
      path: 'menu2detallado',
      component: () => import('@/views/informe/menu2detallado/index'), // Parent router-view
      name: 'menu_informe_menu2detallado',
      meta: { title: 'menu_informe_menu2detallado', icon: 'el-icon-document', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'ingeniero', 'supervisor'] },
      redirect: '/informe/menu2detallado/menu2-1material',
      children: [
        {
          path: 'menu2-1material',
          component: () => import('@/views/informe/menu2detallado/menu2-1material'),
          name: 'menu_informe_menu2detallado_menu2-1material',
          meta: { title: 'menu_informe_menu2detallado_menu2-1material', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-2expansion',
          component: () => import('@/views/informe/menu2detallado/menu2-2expansion'),
          name: 'menu_informe_menu2detallado_menu2-2expansion',
          meta: { title: 'menu_informe_menu2detallado_menu2-2expansion', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-3reubicacion',
          component: () => import('@/views/informe/menu2detallado/menu2-3reubicacion'),
          name: 'menu_informe_menu2detallado_menu2-3reubicacion',
          meta: { title: 'menu_informe_menu2detallado_menu2-3reubicacion', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-4modernizacion',
          component: () => import('@/views/informe/menu2detallado/menu2-4modernizacion'),
          name: 'menu_informe_menu2detallado_menu2-4modernizacion',
          meta: { title: 'menu_informe_menu2detallado_menu2-4modernizacion', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-5actualizacion',
          component: () => import('@/views/informe/menu2detallado/menu2-5actualizacion'),
          name: 'menu_informe_menu2detallado_menu2-5actualizacion',
          meta: { title: 'menu_informe_menu2detallado_menu2-5actualizacion', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-6reposicion',
          component: () => import('@/views/informe/menu2detallado/menu2-6reposicion'),
          name: 'menu_informe_menu2detallado_menu2-6reposicion',
          meta: { title: 'menu_informe_menu2detallado_menu2-6reposicion', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-7repotenciacion',
          component: () => import('@/views/informe/menu2detallado/menu2-7repotenciacion'),
          name: 'menu_informe_menu2detallado_menu2-7repotenciacion',
          meta: { title: 'menu_informe_menu2detallado_menu2-7repotenciacion', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-8retiro',
          component: () => import('@/views/informe/menu2detallado/menu2-8retiro'),
          name: 'menu_informe_menu2detallado_menu2-8retiro',
          meta: { title: 'menu_informe_menu2detallado_menu2-8retiro', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-9cambiomedida',
          component: () => import('@/views/informe/menu2detallado/menu2-9cambiomedida'),
          name: 'menu_informe_menu2detallado_menu2-9cambiomedida',
          meta: { title: 'menu_informe_menu2detallado_menu2-9cambiomedida', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-10retirovsreubicacion',
          component: () => import('@/views/informe/menu2detallado/menu2-10retirovsreubicacion'),
          name: 'menu_informe_menu2detallado_menu2-10retirovsreubicacion',
          meta: { title: 'menu_informe_menu2detallado_menu2-10retirovsreubicacion', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-11porbarrio',
          component: () => import('@/views/informe/menu2detallado/menu2-11porbarrio'),
          name: 'menu_informe_menu2detallado_menu2-11porbarrio',
          meta: { title: 'menu_informe_menu2detallado_menu2-11porbarrio', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-12repetido',
          component: () => import('@/views/informe/menu2detallado/menu2-12repetido'),
          name: 'menu_informe_menu2detallado_menu2-12repetido',
          meta: { title: 'menu_informe_menu2detallado_menu2-12repetido', icon: 'el-icon-info', roles: ['super', 'auxiliar'] }
        },
        {
          path: 'menu2-13cambiodireccion',
          component: () => import('@/views/informe/menu2detallado/menu2-13cambiodireccion'),
          name: 'menu_informe_menu2detallado_menu2-13cambiodireccion',
          meta: { title: 'menu_informe_menu2detallado_menu2-13cambiodireccion', icon: 'el-icon-info', roles: ['super', 'auxiliar', 'admin', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-14luminariareporte',
          component: () => import('@/views/informe/menu2detallado/menu2-14luminariareporte'),
          name: 'menu_informe_menu2detallado_menu2-14luminariareporte',
          meta: { title: 'menu_informe_menu2detallado_menu2-14luminariareporte', icon: 'el-icon-info', roles: ['super', 'auxiliar', 'admin', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-15obracuadrilla',
          component: () => import('@/views/informe/menu2detallado/menu2-15obracuadrilla'),
          name: 'menu_informe_menu2detallado_menu2-15obracuadrilla',
          meta: { title: 'menu_informe_menu2detallado_menu2-15obracuadrilla', icon: 'el-icon-info', roles: ['super', 'auxiliar', 'admin', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu2-16reportesinot',
          component: () => import('@/views/informe/menu2detallado/menu2-16reportesinot'),
          name: 'menu_informe_menu2detallado_menu2-16reportesinot',
          meta: { title: 'menu_informe_menu2detallado_menu2-16reportesinot', icon: 'el-icon-info', roles: ['super', 'auxiliar', 'admin', 'gerencia', 'ingeniero', 'supervisor'] }
        }
      ]
    },
    {
      path: 'menu3inventario',
      component: () => import('@/views/informe/menu3inventario/index'), // Parent router-view
      name: 'menu_informe_menu3inventario',
      meta: { title: 'menu_informe_menu3inventario', icon: 'el-icon-files', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] },
      redirect: '/informe/menu3inventario/menu3-1luminaria',
      children: [
        {
          path: 'menu3-1luminaria',
          component: () => import('@/views/informe/menu3inventario/menu3-1luminaria'),
          name: 'menu_informe_menu3inventario_menu3-1luminaria',
          meta: { title: 'menu_informe_menu3inventario_menu3-1luminaria', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu3-2control',
          component: () => import('@/views/informe/menu3inventario/menu3-2control'),
          name: 'menu_informe_menu3inventario_menu3-2control',
          meta: { title: 'menu_informe_menu3inventario_menu3-2control', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu3-3canalizacion',
          component: () => import('@/views/informe/menu3inventario/menu3-3canalizacion'),
          name: 'menu_informe_menu3inventario_menu3-3canalizacion',
          meta: { title: 'menu_informe_menu3inventario_menu3-3canalizacion', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu3-4poste',
          component: () => import('@/views/informe/menu3inventario/menu3-4poste'),
          name: 'menu_informe_menu3inventario_menu3-4poste',
          meta: { title: 'menu_informe_menu3inventario_menu3-4poste', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu3-5redes',
          component: () => import('@/views/informe/menu3inventario/menu3-5redes'),
          name: 'menu_informe_menu3inventario_menu3-5redes',
          meta: { title: 'menu_informe_menu3inventario_menu3-5redes', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu3-6transformador',
          component: () => import('@/views/informe/menu3inventario/menu3-6transformador'),
          name: 'menu_informe_menu3inventario_menu3-6transformador',
          meta: { title: 'menu_informe_menu3inventario_menu3-6transformador', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu3-7medidor',
          component: () => import('@/views/informe/menu3inventario/menu3-7medidor'),
          name: 'menu_informe_menu3inventario_menu3-7medidor',
          meta: { title: 'menu_informe_menu3inventario_menu3-7medidor', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        }
      ]
    },
    {
      path: 'menu4calculo',
      component: () => import('@/views/informe/menu4calculo/index'), // Parent router-view
      name: 'menu_informe_menu4calculo',
      meta: { title: 'menu_informe_menu4calculo', icon: 'el-icon-data-analysis', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] },
      redirect: '/informe/menu4calculo/menu4-1disponibilidad',
      children: [
        {
          path: 'menu4-1disponibilidad',
          component: () => import('@/views/informe/menu4calculo/menu4-1disponibilidad'),
          name: 'menu_informe_menu4calculo_menu4-1disponibilidad',
          meta: { title: 'menu_informe_menu4calculo_menu4-1disponibilidad', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu4-2carga',
          component: () => import('@/views/informe/menu4calculo/menu4-2carga'),
          name: 'menu_informe_menu4calculo_menu4-2carga',
          meta: { title: 'menu_informe_menu4calculo_menu4-2carga', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu4-3eficiencia',
          component: () => import('@/views/informe/menu4calculo/menu4-3eficiencia'),
          name: 'menu_informe_menu4calculo_menu4-3eficiencia',
          meta: { title: 'menu_informe_menu4calculo_menu4-3eficiencia', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        },
        {
          path: 'menu4-4ucap',
          component: () => import('@/views/informe/menu4calculo/menu4-4ucap'),
          name: 'menu_informe_menu4calculo_menu4-4ucap',
          meta: { title: 'menu_informe_menu4calculo_menu4-4ucap', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        }
      ]
    },
    {
      path: 'menu5grafica',
      component: () => import('@/views/informe/menu5grafica/index'), // Parent router-view
      name: 'menu_informe_menu5grafica',
      meta: { title: 'menu_informe_menu5grafica', icon: 'el-icon-pie-chart', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] },
      redirect: '/informe/menu5grafica/menu5-1operativa',
      children: [
        {
          path: 'menu5-1operativa',
          component: () => import('@/views/informe/menu5grafica/menu5-1operativa'),
          name: 'menu_informe_menu5grafica_menu5-1operativa',
          meta: { title: 'menu_informe_menu5grafica_menu5-1operativa', icon: 'el-icon-info', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
        }
      ]
    },
    {
      path: 'menu6foto',
      component: () => import('@/views/informe/menu6foto/index'), // Parent router-view
      name: 'menu_informe_menu6foto',
      meta: { title: 'menu_informe_menu6foto', icon: 'el-icon-camera', roles: ['super', 'admin', 'auxiliar', 'gerencia', 'ingeniero', 'supervisor'] }
    }
  ]
}

export default informeRouter
