const vistaReporteListaHistorica = {
    path: 'menu4-2-1list',
              component: () =>
                import(
                  '@/views/proceso/menu4orden/menu4-2externa/menu4-2-1list'
                ),
              name: 'menu_proceso_menu4orden_menu4-1externa_menu4-2-1list',
              hidden: true,
              meta: {
                title: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-1list',
                roles: ['super', 'admin', 'ingeniero', 'supervisor']
              }
}
export default vistaReporteListaHistorica