/** When your routing table is too long, you can split it into small modules**/
import Modulo from '@/views/grupo/role/modulo'

const _import = require('../_import_' + process.env.NODE_ENV)

const supervisorRouter = {
  path: '/supervisor',
  component: Modulo,
  redirect: 'noredirect',
  name: 'supervisor',
  single: false,
  meta: {
    title: 'supervisor',
    icon: 'el-icon-unlock',
    roles: ['super', 'admin', 'gerencia', 'supervisor']
  },
  children: [
    { path: 'crearaap', component: _import('supervisor/crearaap/index'), name: 'crearaap', meta: { title: 'crearaap', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor'] } },
    { path: 'recuperaraap', component: _import('supervisor/recuperaraap/index'), name: 'recuperaraap', meta: { title: 'recuperaraap', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor'] } },
    { path: 'abrirreporte', component: _import('supervisor/abrirreporte/index'), name: 'abrirreporte', meta: { title: 'abrirreporte', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor'] } },
    { path: 'cargarreporte', component: _import('supervisor/cargarreporte/index'), name: 'cargarreporte', meta: { title: 'cargarreporte', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor'] } }
  ]
}

export default supervisorRouter
