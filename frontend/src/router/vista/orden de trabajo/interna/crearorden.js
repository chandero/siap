const vistaReporteOrdenCrear = {
    path: 'menu4-1-2create',
              component: () =>
                import(
                  '@/views/proceso/menu4orden/menu4-1interna/menu4-1-2create'
                ),
              name: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-2create',
              hidden: true,
              meta: {
                title: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-2create',
                roles: ['super', 'admin', 'pqrs', 'supervisor']
              }
}
export default vistaReporteOrdenCrear