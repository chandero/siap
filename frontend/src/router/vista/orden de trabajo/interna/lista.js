const vistaReporteOrdenLista = {
    path: 'menu4-1-1list',
              component: () =>
                import(
                  '@/views/proceso/menu4orden/menu4-1interna/menu4-1-1list'
                ),
              name: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-1list',
              hidden: true,
              meta: {
                title: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-1list',
                roles: ['super', 'admin', 'pqrs', 'ingeniero', 'supervisor']
              }
}
export default vistaReporteOrdenLista