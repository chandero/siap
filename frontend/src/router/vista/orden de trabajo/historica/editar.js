const vistaReporteEditarHistorica = {
    path: 'menu4-2-3edit/:id?',
              component: () =>
                import(
                  '@/views/proceso/menu4orden/menu4-2externa/menu4-2-3edit'
                ),
              name: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-3edit',
              hidden: true,
              meta: {
                title: 'menu_proceso_menu4orden_menu4-2externa_menu4-2-3edit',
                roles: ['super', 'pqrs', 'supervisor']
              }
}
export default vistaReporteEditarHistorica