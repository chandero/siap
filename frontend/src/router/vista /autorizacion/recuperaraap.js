const vistaAutorizacionRecuperarReporte = {
    path: 'recuperaraap',
      component: () => import('@/views/autorizacion/recuperaraap/index'),
      name: 'cargarreporte',
      meta: { title: 'Recuperar App', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
}
export default vistaAutorizacionRecuperarReporte