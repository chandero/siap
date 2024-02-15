import vistaInventarioRedesActivo from "@/router/vistaInventarioRedesActivo"
import vistaInventarioRedesCrear from "@/router/vistaInventarioRedesCrear"
import vistaInventarioRedesEditar from "@/router/vistaInventarioRedesEditar"
import vistaInventarioRedesEnBajo from "@/router/vistaInventarioRedesEnBajo"

const menuInventarioRedesPqrs = {
    path: 'menu5redes',
      component: () => import('@/views/inventario/menu5redes/index'), // Parent router-view
      name: 'menu_inventario_menu5redes',
      meta: { title: 'menu_inventario_menu5redes', icon: 'el-icon-phone-outline', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero']
    },
    children: [
        vistaInventarioRedesActivo,
        vistaInventarioRedesCrear,
        vistaInventarioRedesEditar,
        vistaInventarioRedesEnBajo
    ]
}
export default menuInventarioRedesPqrs