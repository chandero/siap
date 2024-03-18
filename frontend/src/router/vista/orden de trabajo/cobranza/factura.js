const vistaReporteCobranzaFactura = {
    path: 'menu4-3-2factura',
              component: () =>
                import(
                  '@/views/proceso/menu4orden/menu4-3cobro/menu4-3-2factura'
                ),
              name: 'menu_proceso_menu4orden_menu4-3cobro_menu4-3-2factura',
              meta: {
                title:
                  'menu_proceso_menu4orden_menu4-3cobro_menu4-3-2factura',
                roles: ['super', 'admin', 'pqrs', 'facturacion']
              }
}
export default vistaReporteCobranzaFactura