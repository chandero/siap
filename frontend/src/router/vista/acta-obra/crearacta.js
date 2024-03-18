const vistaActaObraCrear = {
    path: 'menu2create',
      component: () => import('@/views/acta-obra/menu2create/index'),
      name: 'menu_acta_obra_menu2create',
      hidden: true,
      meta: {
        title: 'Crear',
        noCache: true,
        icon: 'el-icon-document-remove',
        roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor']
      }
}
export default vistaActaObraCrear