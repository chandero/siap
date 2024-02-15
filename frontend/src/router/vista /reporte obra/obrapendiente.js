const vistaReporteObraPendiente = {
    path: 'menu3-4pendiente',
              component: () => import('@/views/proceso/menu3obra/menu3-4pendiente'),
              name: 'menu_proceso_menu3obra_menu3-4pendiente',
              meta: {
                title: 'menu_proceso_menu3obra_menu3-4pendiente',
                icon: 'el-icon-s-unfold',
                roles: ['super', 'admin', 'pqrs', 'ingeniero', 'supervisor']
              }
}
export default vistaReporteObraPendiente