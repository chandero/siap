const vistaReporteMigracionLista = {
    path: 'menu1-99-1list',
              component: () =>
                import(
                  '@/views/proceso/menu1reporte/menu1-99migracion/menu1-99-1list'
                ),
              name: 'menu_proceso_menu1reporte_menu1-99migracion_menu1-99-1list',
              hidden: true,
              meta: {
                title:
                  'menu_proceso_menu1reporte_menu1-99migracion_menu1-99-1list',
                roles: ['super', 'admin', 'pqrs', 'ingeniero', 'supervisor']
              }
}
export default vistaReporteMigracionLista 