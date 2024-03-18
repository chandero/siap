const vistaActaObraItem = {
    path: 'menu4item',
    component: () => import('@/views/acta-obra/menu4item/index'),
    name: 'menu_acta_obra_menu4item',
    hidden: false,
    meta: {
      title: 'Iten',
      noCache: true,
      icon: 'el-icon-document-remove',
      roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor']
    }
}
export default vistaActaObraItem