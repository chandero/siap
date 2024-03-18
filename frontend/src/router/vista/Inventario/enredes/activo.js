const vistaInventarioRedesActivo = {
    path: 'menu5-1activa',
          component: () => import('@/views/inventario/menu5redes/menu5-1activa'),
          name: 'menu_inventario5.1.2',
          meta: { title: 'menu_inventario_menu5redes_menu5-1activa',
          icon: 'el-icon-sunrise', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
}
export default vistaInventarioRedesActivo