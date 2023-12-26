import Modulo from '@/views/grupo/role/modulo'
const calculorouter = {
  path: '/calculo',
  component: Modulo,
  redirect: 'noredirect',
  single: false,
  name: 'calculo',
  meta: { title: 'Calculo', icon: 'el-icon-search', roles: ['super', 'admin', 'supervisor', 'fact'] },
  children: [
    {
      path: 'menu4-2carga',
      component: () => import('@/views/informe/menu4calculo/menu4-2carga/index'),
      name: 'menu4-2carga',
      meta: { title: 'Carga', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
    },
    {
      path: 'menu4-3eficiencia',
      component: () => import('@/views/informe/menu4calculo/menu4-3eficiencia/index'),
      name: 'abrirreporte',
      meta: { title: 'Eficiencia', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
    }
  ]
}
export default calculorouter
