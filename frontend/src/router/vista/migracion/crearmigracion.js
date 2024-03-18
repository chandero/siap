const vistaReporteMigracionCrear = {
    path: 'menu1-99-2create/:tireuc_id',
              component: () =>
                import(
                  '@/views/proceso/menu1reporte/menu1-99migracion/menu1-99-2create'
                ),
              name:
                'menu_proceso_menu1reporte_menu1-99migracion_menu1-99-2create',
              hidden: true,
              meta: {
                title:
                  'menu_proceso_menu1reporte_menu1-99migracion_menu1-99-2create',
                roles: ['super', 'pqrs', 'supervisor']
              }
}
export default vistaReporteMigracionCrear