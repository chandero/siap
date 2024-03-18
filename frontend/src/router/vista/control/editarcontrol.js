const vistaReporteControlEditar = {
    path: 'menu1-2-3edit/:id',
              component: () =>
                import(
                  '@/views/proceso/menu1reporte/menu1-2control/menu1-2-3edit'
                ),
              name: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-3create',
              hidden: true,
              meta: {
                title: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-3edit',
                roles: ['super', 'pqrs', 'supervisor']
              }
}
export default vistaReporteControlEditar