const vistaAutorizacionCrearReporte = {
    path: 'crearaap',
      component: () => import('@/views/autorizacion/crearaap/index'),
      name: 'crearaap',
      meta: { title: 'Crear App', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
}
export default vistaAutorizacionCrearReporte