import vistaInventarioMedidor from "@/router/vistaInventarioMedidor"
const menuInventarioMedidorGerencia = {
    path: 'menu7medidor',
      component: () => import('@/views/inventario/menu7medidor/index'),
      name: 'menu_inventario_menu7medidor',
      meta: { title: 'menu_inventario_menu7medidor', icon: 'el-icon-c-scale-to-original', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero']
    },
    children: [
        vistaInventarioMedidor
    ]
}
export default menuInventarioMedidorGerencia