/** When your routing table is too long, you can split it into small modules**/
import Modulo from '@/views/grupo/role/modulo'

const _import = require('../_import_' + process.env.NODE_ENV)

const gerenciaRouter = {
  path: '/gerencia',
  component: Modulo,
  redirect: 'noredirect',
  name: 'gerencia',
  single: false,
  meta: {
    title: 'gerencia',
    icon: 'el-icon-s-management',
    roles: ['super', 'admin', 'ingeniero', 'gerencia']
  },
  children: [
    { path: 'grafica', component: _import('gerencia/grafica'), name: 'grafica', meta: { title: 'grafica', roles: ['super', 'admin', 'ingeniero', 'gerencia'] } },
    { path: 'reporteporuso', component: _import('gerencia/reporteporuso'), name: 'reporteporuso', meta: { title: 'reporteporuso', roles: ['super', 'admin', 'ingeniero', 'gerencia'] } }
  ]
}

export default gerenciaRouter
