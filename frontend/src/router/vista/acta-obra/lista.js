const vistaActaObraLista = {
    path: 'menu1list',
      component: () => import('@/views/acta-obra/menu1list/index'),
      name: 'menu_acta_obra_menu1list',
      meta: {
        title: 'Lista',
        noCache: true,
        icon: 'el-icon-document-remove',
        roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor']
      }
}
export default vistaActaObraLista