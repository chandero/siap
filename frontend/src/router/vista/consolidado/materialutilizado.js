const VistaInformeConsolidadoMaterialUtilizado = {
    path: 'menu1consolidado',
      component: () => import('@/views/informe/menu1consolidado/index'), // Parent router-view
      name: 'menu_consolidado1.1.1',
      meta: { title: 'Informe de Material', icon: 'el-icon-document-remove', roles: ['super', 'pqrs', 'gerencia', 'ingeniero', 'supervisor', 'interventoria'] },
}
export default VistaInformeConsolidadoMaterialUtilizado