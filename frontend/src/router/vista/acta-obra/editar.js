const vistaActaObraEditar = {
    path: 'menu3edit/:id',
      component: () => import('@/views/acta-obra/menu3edit/index'),
      name: 'menu_acta_obra_menu3edit',
      hidden: true,
      meta: {
        title: 'Editar',
        noCache: true,
        icon: 'el-icon-document-remove',
        roles: ['super', 'admin', 'auxiliar', 'ingeniero', 'supervisor']
      }
}
export default vistaActaObraEditar