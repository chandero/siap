const vistaReporteControlListaLuminarias = {
    path: 'menu1-2-1list',
              component: () =>
                import(
                  '@/views/proceso/menu1reporte/menu1-2control/menu1-2-1list'
                ),
              name: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-1list',
              hidden: true,
              meta: {
                title: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-1list',
                roles: ['super', 'admin', 'pqrs', 'ingeniero', 'supervisor']
              }
}
export default vistaReporteControlListaLuminarias