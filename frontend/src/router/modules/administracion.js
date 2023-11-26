/** When your routing table is too long, you can split it into small modules**/
import Modulo from '@/views/grupo/role/modulo'

const _import = require('../_import_' + process.env.NODE_ENV)

const administracionRouter = {
  path: '/administracion',
  component: Modulo,
  redirect: 'noredirect',
  name: 'administracion',
  single: false,
  meta: {
    title: 'administracion',
    icon: 'el-icon-setting',
    roles: ['super', 'admin', 'ingeniero', 'pqrs']
  },
  children: [
    {
      path: 'ucap',
      component: _import('administracion/ucap'),
      name: 'ucap',
      meta: { title: 'ucap', roles: ['super'] }
    },
    {
      path: 'ucap/editar/:id',
      component: _import('administracion/ucap/edit'),
      name: 'ucapedit',
      hidden: true,
      redirect: false,
      meta: { title: 'ucapedit', roles: ['super'] }
    },
    {
      path: 'ucap/crear',
      component: _import('administracion/ucap/create'),
      name: 'ucapcreate',
      hidden: true,
      meta: { title: 'ucapcreate', roles: ['super'] }
    },
    {
      path: 'unitario',
      component: _import('administracion/unitario'),
      name: 'unitario',
      meta: { title: 'unitario', roles: ['super', 'admin'] }
    },
    {
      path: 'unitario/editar/:id',
      component: _import('administracion/unitario/edit'),
      name: 'unitarioedit',
      hidden: true,
      redirect: false,
      meta: { title: 'unitarioedit', roles: ['super'] }
    },
    {
      path: 'unitario/crear',
      component: _import('administracion/unitario/create'),
      name: 'unitariocreate',
      hidden: true,
      meta: { title: 'unitariocreate', roles: ['super'] }
    },
    {
      path: 'unidad',
      component: _import('administracion/unidad'),
      name: 'unidad',
      meta: { title: 'unidad', roles: ['super'] }
    },
    {
      path: 'unidad/editar/:id',
      component: _import('administracion/unidad/edit'),
      name: 'unidadedit',
      hidden: true,
      redirect: false,
      meta: { title: 'unidadedit', roles: ['super'] }
    },
    {
      path: 'unidad/crear',
      component: _import('administracion/unidad/create'),
      name: 'unidadcreate',
      hidden: true,
      meta: { title: 'unidadcreate', roles: ['super'] }
    },
    {
      path: 'caracteristica',
      component: _import('administracion/caracteristica'),
      name: 'caracteristica',
      meta: { title: 'caracteristica', roles: ['super'] }
    },
    {
      path: 'caracteristica/editar/:id',
      component: _import('administracion/caracteristica/edit'),
      name: 'caracteristicaedit',
      hidden: true,
      redirect: false,
      meta: { title: 'caracteristicaedit', roles: ['super'] }
    },
    {
      path: 'caracteristica/crear',
      component: _import('administracion/caracteristica/create'),
      name: 'caracteristicacreate',
      hidden: true,
      meta: { title: 'caracteristicacreate', roles: ['super'] }
    },
    {
      path: 'tipoelemento',
      component: _import('administracion/tipoelemento'),
      name: 'tipoelemento',
      meta: { title: 'tipoelemento', roles: ['super'] }
    },
    {
      path: 'tipoelemento/editar/:id',
      component: _import('administracion/tipoelemento/edit'),
      name: 'tipoelementoedit',
      hidden: true,
      redirect: false,
      meta: { title: 'tipoelementoedit', roles: ['super'] }
    },
    {
      path: 'tipoelemento/crear',
      component: _import('administracion/tipoelemento/create'),
      name: 'tipoelementocreate',
      hidden: true,
      meta: { title: 'tipoelementocreate', roles: ['super'] }
    },
    // ELEMENTO
    {
      path: 'elemento',
      component: () => import('@/views/administracion/elemento/index'),
      redirect: '/administracion/elemento/list',
      name: 'elemento',
      meta: {
        title: 'elemento.elemento',
        icon: 'el-icon-document',
        roles: [
          'super',
          'admin',
          'pqrs',
          'gerencia',
          'ingeniero',
          'supervisor'
        ]
      },
      children: [
        {
          path: 'ipp',
          component: () =>
            import('@/views/administracion/elemento/menu04/index'),
          name: 'ucap_ipp',
          meta: {
            title: 'ipp.lista',
            icon: 'el-icon-notebook-2',
            roles: ['super', 'admin', 'supervisor']
          }
        },
        {
          path: 'list',
          component: () =>
            import('@/views/administracion/elemento/menu00/index'),
          name: 'elemento_lista',
          meta: {
            title: 'elemento.lista',
            icon: 'el-icon-info',
            roles: [
              'super',
              'admin',
              'pqrs',
              'gerencia',
              'ingeniero',
              'supervisor'
            ]
          }
        },
        {
          path: 'price',
          component: () =>
            import('@/views/administracion/elemento/menu03/index'),
          name: 'elemento_precio',
          meta: {
            title: 'elemento.precio',
            icon: 'el-icon-money',
            roles: ['super', 'admin', 'supervisor']
          }
        },
        {
          path: 'editar/:id',
          component: _import('administracion/elemento/menu02/index'),
          name: 'elementoedit',
          hidden: true,
          redirect: false,
          meta: {
            title: 'elemento.elementoedit',
            roles: ['super', 'admin', 'ingeniero', 'pqrs']
          }
        },
        {
          path: 'crear',
          component: _import('administracion/elemento/menu01/index'),
          name: 'elementocreate',
          hidden: true,
          meta: {
            title: 'elemento.elementocreate',
            roles: ['super', 'admin', 'ingeniero', 'pqrs']
          }
        }
      ]
    }, // FIN ELEMENTO
    /* { path: 'elemento', component: _import('administracion/elemento/elemento'), name: 'elemento', meta: { title: 'elemento', roles: ['super', 'admin', 'ingeniero', 'pqrs'] } },
    { path: 'elemento/editar/:id', component: _import('administracion/elemento/edit'), name: 'elementoedit', hidden: true, redirect: false, meta: { title: 'elementoedit', roles: ['super', 'admin', 'ingeniero', 'pqrs'] } },
    { path: 'elemento/crear', component: _import('administracion/elemento/create'), name: 'elementocreate', hidden: true, meta: { title: 'elementocreate', roles: ['super', 'admin', 'ingeniero', 'pqrs'] } },
    */ {
      path: 'maob',
      component: () => import('@/views/administracion/manoobraorden/index'),
      redirect: '/administracion/manoobraorden/menu01',
      name: 'mano_obra',
      meta: {
        title: 'mano_obra.title',
        icon: 'el-icon-document',
        roles: [
          'super',
          'admin',
          'gerencia',
          'ingeniero',
          'supervisor'
        ]
      },
      children: [
        {
          path: 'list',
          component: () =>
            import('@/views/administracion/manoobraorden/menu01/index'),
          name: 'mano_obra_lista',
          meta: {
            title: 'mano_obra.lista',
            icon: 'el-icon-info',
            roles: [
              'super',
              'admin',
              'pqrs',
              'gerencia',
              'ingeniero',
              'supervisor'
            ]
          }
        },
        {
          path: 'price',
          component: () =>
            import('@/views/administracion/manoobraorden/menu02/index'),
          name: 'mano_obra_precio',
          meta: {
            title: 'mano_obra.precio',
            icon: 'el-icon-money',
            roles: ['super', 'admin', 'supervisor']
          }
        }
      ]
    },
    {
      path: 'math',
      component: () =>
        import('@/views/administracion/manoherramientaorden/index'),
      redirect: '/administracion/manoherramientaorden/menu01',
      name: 'mano_herramienta',
      meta: {
        title: 'mano_herramienta.title',
        icon: 'el-icon-document',
        roles: [
          'super',
          'admin',
          'gerencia',
          'ingeniero',
          'supervisor'
        ]
      },
      children: [
        {
          path: 'list',
          component: () =>
            import('@/views/administracion/manoherramientaorden/menu01/index'),
          name: 'mano_herramienta_lista',
          meta: {
            title: 'mano_herramienta.lista',
            icon: 'el-icon-info',
            roles: [
              'super',
              'admin',
              'pqrs',
              'gerencia',
              'ingeniero',
              'supervisor'
            ]
          }
        },
        {
          path: 'price',
          component: () =>
            import('@/views/administracion/manoherramientaorden/menu02/index'),
          name: 'mano_herramienta_precio',
          meta: {
            title: 'mano_herramienta.precio',
            icon: 'el-icon-money',
            roles: ['super', 'admin', 'supervisor']
          }
        }
      ]
    },
    /* { path: 'manoobra', component: _import('administracion/manoobra'), name: 'manoobra', meta: { title: 'manoobra', roles: ['super', 'admin', 'almacen'] } },
    { path: 'manoobra/editar/:id', component: _import('administracion/manoobra/edit'), name: 'manoobraedit', hidden: true, redirect: false, meta: { title: 'manoobraedit', roles: ['super', 'admin', 'almacen'] } },
    { path: 'manoobra/crear', component: _import('administracion/manoobra/create'), name: 'manoobracreate', hidden: true, meta: { title: 'manoobracreate', roles: ['super', 'admin', 'almacen'] } },
    */
    {
      path: 'contratista',
      component: _import('administracion/contratista/index'),
      name: 'contratista',
      meta: {
        title: 'Contratista',
        roles: ['super', 'admin', 'ingeniero', 'supervisor']
      }
    },
    {
      path: 'contratista/editar/:id',
      component: _import('administracion/contratista/edit'),
      name: 'contratistaedit',
      hidden: true,
      redirect: false,
      meta: {
        title: 'Contratista Editar',
        roles: ['super', 'admin', 'ingeniero', 'supervisor']
      }
    },
    {
      path: 'contratista/crear',
      component: _import('administracion/contratista/create'),
      name: 'contratistacreate',
      hidden: true,
      meta: {
        title: 'Contratista Crear',
        roles: ['super', 'admin', 'ingeniero', 'supervisor']
      }
    },
    {
      path: 'cuadrilla',
      component: _import('administracion/cuadrilla'),
      name: 'cuadrilla',
      meta: { title: 'cuadrilla', roles: ['super', 'admin', 'ingeniero', 'supervisor'] }
    },
    {
      path: 'cuadrilla/editar/:id',
      component: _import('administracion/cuadrilla/edit'),
      name: 'cuadrillaedit',
      hidden: true,
      redirect: false,
      meta: { title: 'cuadrillaedit', roles: ['super', 'admin', 'ingeniero', 'supervisor'] }
    },
    {
      path: 'cuadrilla/crear',
      component: _import('administracion/cuadrilla/create'),
      name: 'cuadrillacreate',
      hidden: true,
      meta: { title: 'cuadrillacreate', roles: ['super', 'admin', 'ingeniero', 'supervisor'] }
    },
    {
      path: 'usuario',
      component: _import('administracion/usuario'),
      name: 'usuario',
      meta: { title: 'usuario', roles: ['super', 'admin'] }
    },
    {
      path: 'usuario/crear',
      component: _import('administracion/usuario/create'),
      name: 'usuariocreate',
      hidden: true,
      meta: { title: 'usuariocreate', roles: ['super', 'admin'] }
    },
    {
      path: 'usuario/editar/:id',
      component: _import('administracion/usuario/edit'),
      name: 'usuarioedit',
      hidden: true,
      meta: { title: 'usuarioedit', roles: ['super', 'admin'] }
    },
    {
      path: 'empresa',
      component: _import('administracion/empresa'),
      name: 'empresa',
      meta: { title: 'empresa', roles: ['super'] }
    },
    {
      path: 'empresa/editar/:id',
      component: _import('administracion/empresa/edit'),
      name: 'empresaedit',
      hidden: true,
      meta: { title: 'empresaedit', roles: ['super'] }
    },
    {
      path: 'empresa/crear',
      component: _import('administracion/empresa/create'),
      name: 'empresacreate',
      hidden: true,
      meta: { title: 'empresacreate', roles: ['super'] }
    },
    {
      path: 'barrio/lista/:did?/:mid?',
      component: _import('administracion/barrio'),
      name: 'barrio',
      meta: {
        title: 'barrio',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
    },
    {
      path: 'barrio/editar/:id',
      component: _import('administracion/barrio/edit'),
      name: 'barrioedit',
      hidden: true,
      redirect: false,
      meta: {
        title: 'barrioedit',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
    },
    {
      path: 'barrio/crear/:did/:mid',
      component: _import('administracion/barrio/create'),
      name: 'barriocreate',
      hidden: true,
      meta: {
        title: 'barriocreate',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
    },
    {
      path: 'medidor',
      component: _import('administracion/medidor'),
      name: 'medidor',
      meta: {
        title: 'medidor',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
    },
    {
      path: 'medidor/editar/:id',
      component: _import('administracion/medidor/edit'),
      name: 'medidoredit',
      hidden: true,
      meta: {
        title: 'medidoredit',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
    },
    {
      path: 'medidor/crear',
      component: _import('administracion/medidor/create'),
      name: 'medidorcreate',
      hidden: true,
      meta: {
        title: 'medidorcreate',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
    },
    /* { path: 'transformador', component: _import('administracion/transformador'), name: 'transformador', meta: { title: 'transformador', roles: ['super', 'admin', 'ingeniero', 'pqrs'] } },
    { path: 'transformador/editar/:id', component: _import('administracion/transformador/edit'), name: 'transformadoredit', hidden: true, meta: { title: 'transformadoredit', roles: ['super', 'admin', 'ingeniero', 'pqrs'] } },
    { path: 'transformador/crear', component: _import('administracion/transformador/create'), name: 'transformadorcreate', hidden: true, meta: { title: 'transformadorcreate', roles: ['super', 'admin', 'ingeniero', 'pqrs'] } }, */
    {
      path: 'urbanizadora',
      component: _import('administracion/urbanizadora'),
      name: 'urba',
      meta: {
        title: 'urba',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
    },
    {
      path: 'urbanizadora/editar/:id',
      component: _import('administracion/urbanizadora/edit'),
      name: 'urbaedit',
      hidden: true,
      meta: {
        title: 'urbaedit',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
    },
    {
      path: 'urbanizadora/crear',
      component: _import('administracion/urbanizadora/create'),
      name: 'urbacreate',
      hidden: true,
      meta: {
        title: 'urbacreate',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
    }
  ]
}

export default administracionRouter
