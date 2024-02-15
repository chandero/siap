const vistaAutorizacionCargarReporte = {
    path: 'cargarreporte',
      component: () => import('@/views/autorizacion/cargarreporte/index'),
      name: 'cargarreporte',
      meta: { title: 'Cargar Reporte', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
}
export default vistaAutorizacionCargarReporte