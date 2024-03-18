const vistaReporteMigracionEditar = {
    path: 'menu1-99-3edit/:tireuc_id/:id?',
              component: () =>
                import(
                  '@/views/proceso/menu1reporte/menu1-99migracion/menu1-99-3edit'
                ),
              name: 'menu_proceso_menu1reporte_menu1-99migracion_menu1-99-3edit',
              hidden: true,
              meta: {
                title:
                  'menu_proceso_menu1reporte_menu1-99migracion_menu1-99-3edit',
                roles: ['super', 'pqrs', 'supervisor']
              }
}
export default vistaReporteMigracionEditar