const vistaAutorizacionAbrirReporte = {
    path: 'abrirreporte',
      component: () => import('@/views/autorizacion/abrirreporte/index'),
      name: 'abrirreporte',
      meta: { title: 'Abrir Reporte', icon: 'el-icon-info', roles: ['super', 'admin', 'supervisor', 'fact'] }
}
export default vistaAutorizacionAbrirReporte