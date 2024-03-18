const vistaReporteTransformadorLista = {
    path: 'menu1-6-1list',
              component: () =>
                import(
                  '@/views/proceso/menu1reporte/menu1-6transformador/menu1-6-1list'
                ),
              name:
                'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-1list',
              hidden: true,
              meta: {
                title:
                  'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-1list',
                roles: ['super', 'admin', 'pqrs', 'ingeniero', 'supervisor']
              }
}
export default vistaReporteTransformadorLista