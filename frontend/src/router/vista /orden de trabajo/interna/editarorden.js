const vistaReporteOrdenEditar = {
    path: 'menu4-1-3edit/:id',
              component: () =>
                import(
                  '@/views/proceso/menu4orden/menu4-1interna/menu4-1-3edit'
                ),
              name: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-3edit',
              hidden: true,
              meta: {
                title: 'menu_proceso_menu4orden_menu4-1interna_menu4-1-3edit',
                roles: ['super', 'admin', 'pqrs', 'supervisor']
              }
}
export default vistaReporteOrdenEditar