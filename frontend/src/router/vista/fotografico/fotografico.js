const vistaFotografico = {
    path: 'menu6foto',
    component: () => import('@/views/informe/menu6foto/index'), // Parent router-view
    name: 'menu_consolidado6.1.1',
    meta: { title: 'menu_informe_menu6foto', icon: 'el-icon-camera', roles: ['super', 'pqrs', 'gerencia', 'ingeniero', 'interventoria'] }
}
export default vistaFotografico