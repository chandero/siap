import Modulo from '@/views/grupo/role/modulo'
const autorizacionrouter = {
  path: '/autorizacion',
  component: Modulo,
  redirect: 'noredirect',
  single: false,
  name: 'autorizacion',
  meta: { title: 'Autorizacion', icon: 'el-icon-search', roles: ['super', 'admin', 'supervisor', 'fact'] },
  children: [
    {
      path: 'abrirreporte',
      component: () => import('@/views/autorizacion/abrirreporte/index'),
      name: 'abrirreporte',
      meta: { title: 'Abrir Reporte', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
    },
    {
      path: 'cargarreporte',
      component: () => import('@/views/autorizacion/cargarreporte/index'),
      name: 'cargarreporte',
      meta: { title: 'Cargar Reporte', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
    },
    {
      path: 'crearaap',
      component: () => import('@/views/autorizacion/crearaap/index'),
      name: 'crearaap',
      meta: { title: 'Crear App', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
    },
    {
      path: 'recuperaraap',
      component: () => import('@/views/autorizacion/recuperaraap/index'),
      name: 'cargarreporte',
      meta: { title: 'Recuperar App', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
    }
  ]
}

export default autorizacionrouter
