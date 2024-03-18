const vistaReporteInformeCambios = {
    path: 'menu2-3informecambios',
              component: () => import('@/views/proceso/menu2pendiente/menu2-3informecambios/index'), // Parent router-view
              name: 'menu_proceso_menu2pendiente_menu2-3informecambios',
              meta: {
                title: 'menu_proceso_menu2pendiente_menu2-3informecambios',
                icon: 'el-icon-document-copy',
                roles: [
                  'super',
                  'admin',
                  'supervisor'
                ]
              }
}
export default vistaReporteInformeCambios