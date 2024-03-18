const vistaReporteControlCrear = {
    path: 'menu1-2-2create/:tireuc_id',
              component: () =>
                import(
                  '@/views/proceso/menu1reporte/menu1-2control/menu1-2-2create'
                ),
              name: 'menu_proceso_menu1reporte_menu1-2control_menu1-2-2create',
              hidden: true,
              meta: {
                title:
                  'menu_proceso_menu1reporte_menu1-2control_menu1-2-2create',
                roles: ['super', 'pqrs', 'supervisor']
              }
}
export default vistaReporteControlCrear