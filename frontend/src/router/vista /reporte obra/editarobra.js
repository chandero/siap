const vistaReporteObraEditar = {
    path: 'menu3-3edit/:id',
              component: () => import('@/views/proceso/menu3obra/menu3-3edit'),
              name: 'menu_proceso_menu3obra_menu3-3edit',
              hidden: true,
              meta: {
                title: 'menu_proceso_menu3obra_menu3-3edit',
                roles: ['super', 'pqrs', 'supeor']
              }
}
export default vistaReporteObraEditar