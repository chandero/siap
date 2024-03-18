const vistaReporteFormato = {
    path: 'menu5formato',
    component: () => import('@/views/proceso/menu5formato/index'), // Parent router-view
    name: 'menu_proceso_menu5formato',
    meta: {
      title: 'menu_proceso_menu5formato',
      icon: 'el-icon-edit-outline',
      roles: ['super', 'pqrs']
    }
}
export default vistaReporteFormato