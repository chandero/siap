const vistaReportelistaCobranza = {
    path: 'menu4-3-1list',
              component: () =>
                import('@/views/proceso/menu4orden/menu4-3cobro/menu4-3-1list'),
              name: 'menu_proceso_menu4orden_menu4-3cobro_menu4-3-1list',
              meta: {
                title: 'menu_proceso_menu4orden_menu4-3cobro_menu4-3-1list',
                roles: ['super', 'admin', 'pqrs', 'facturacion']
              }
}
export default vistaReportelistaCobranza