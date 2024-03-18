const vistaReporteTransformadorEditar = {
    path: 'menu1-6-3edit/:id',
              component: () =>
                import(
                  '@/views/proceso/menu1reporte/menu1-6transformador/menu1-6-3edit'
                ),
              name:
                'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-3create',
              hidden: true,
              meta: {
                title:
                  'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-3edit',
                roles: ['super', 'pqrs', 'supervisor']
              }
}
export default vistaReporteTransformadorEditar