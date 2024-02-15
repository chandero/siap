const vistaReporteTransformadorCrear = {
    path: 'menu1-6-2create/:tireuc_id',
              component: () =>
                import(
                  '@/views/proceso/menu1reporte/menu1-6transformador/menu1-6-2create'
                ),
              name:
                'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-2create',
              hidden: true,
              meta: {
                title:
                  'menu_proceso_menu1reporte_menu1-6transformador_menu1-6-2create',
                roles: ['super', 'pqrs', 'supervisor']
              }
}
export default vistaReporteTransformadorCrear