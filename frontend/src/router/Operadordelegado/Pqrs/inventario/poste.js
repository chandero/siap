import vistaInventarioPosteActivo from "@/router/vistaInventarioPosteActivo"
import vistaInventarioPosteCrear from "@/router/vistaInventarioPosteCrear"
import vistaInventarioPosteEditar from "@/router/vistaInventarioPosteEditar"
import vistaInventartarioLuminariaEnBaja from "@/router/vistaInventartarioLuminariaEnBaja"

const menuInventarioPostePqrs = {
    path: 'menu4poste',
      component: () => import('@/views/inventario/menu4poste/index'), // Parent router-view
      name: 'menu_inventario_menu4poste',
      meta: { title: 'menu_inventario_menu4poste', icon: 'el-icon-wind-power', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero']
    },
    clidren: [
        vistaInventarioPosteActivo,
        vistaInventarioPosteCrear,
        vistaInventarioPosteEditar,
        vistaInventartarioLuminariaEnBaja
    ]
}
export default menuInventarioPostePqrs