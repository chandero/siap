const vistaReporteCobranzaActa = {
    path: 'menu4-3-3acta',
    component: () =>
      import(
        '@/views/proceso/menu4orden/menu4-3cobro/menu4-3-3acta'
      ),
    name: 'menu_proceso_menu4orden_menu4-3cobro_menu4-3-3acta',
    meta: {
      title:
        'menu_proceso_menu4orden_menu4-3cobro_menu4-3-3acta',
      roles: ['super', 'admin', 'pqrs', 'facturacion']
    }
}
export default vistaReporteCobranzaActa