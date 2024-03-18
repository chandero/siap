const vistaReporteInformeEjecutados = {
    path: 'menu2-2ejecutado',
              component: () =>
                import('@/views/proceso/menu2pendiente/menu2-2ejecutado'),
              name: 'menu_proceso_menu2pendiente_menu2-2ejecutado',
              meta: {
                title: 'menu_proceso_menu2pendiente_menu2-2ejecutado',
                icon: 'el-icon-document-checked',
                roles: ['super', 'admin', 'pqrs', 'ingeniero', 'supervisor']
              }
}
export default vistaReporteInformeEjecutados