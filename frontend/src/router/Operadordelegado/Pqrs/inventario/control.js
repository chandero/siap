import vistaReporteControlCrear from "@/router/vistaReporteControlCrear"
import vistaReporteControlEditar from "@/router/vistaReporteControlEditar"
import vistaReporteControlListaLuminarias from "@/router/vistaReporteControlListaLuminarias"

const menuInventarioControlPqrs = {
    path: 'menu2control',
      component: () => import('@/views/inventario/menu2control/index'), // Parent router-view
      name: 'menu_inventario_menu2control',
      meta: { title: 'menu_inventario_menu2control', icon: 'el-icon-watermelon', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero']
    },
    children: [
        vistaReporteControlCrear,
        vistaReporteControlEditar,
        vistaReporteControlListaLuminarias
    ]
}
export default menuInventarioControlPqrs