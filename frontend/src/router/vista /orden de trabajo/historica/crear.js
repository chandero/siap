const vistaReporteCrearHistorica = {
    path: 'menu4-2-2create',
              component: () =>
                import(
                  '@/views/proceso/menu4orden/menu4-2externa/menu4-2-2create'
                ),
              name: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-2create',
              hidden: true,
              meta: {
                title: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-2create',
                roles: ['super', 'admin', 'pqrs', 'ingeniero', 'supervisor']
              }
}
export default vistaReporteCrearHistorica