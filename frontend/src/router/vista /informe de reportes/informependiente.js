const vistaReporteInformePendiente = {
    path: 'menu2-1pendiente',
    component: () =>
      import('@/views/proceso/menu2pendiente/menu2-1pendiente'),
    name: 'menu_proceso_menu2pendiente_menu2-1pendiente',
    meta: {
      title: 'menu_proceso_menu2pendiente_menu2-1pendiente',
      icon: 'el-icon-edit-outline',
      roles: ['super', 'pqrs', 'ingeniero', 'supervisor']
    }
}
export default vistaReporteInformePendiente