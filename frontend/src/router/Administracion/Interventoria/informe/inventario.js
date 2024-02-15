import vistaInformeInventarioCanalizacion from "@/router/vistaInformeInventarioCanalizacion"
import vistaInformeInventarioControl from "@/router/vistaInformeInventarioControl"
import vistaInformeInventarioLuminaria from "@/router/vistaInformeInventarioLuminaria"
import vistaInformeInventarioMedidor from "@/router/vistaInformeInventarioMedidor"
import vistaInformeInventarioPoste from "@/router/vistaInformeInventarioPoste"
import vistaInformeInventarioRedes from "@/router/vistaInformeInventarioRedes"
import vistaInformeInventarioTRansformador from "@/router/vistaInformeInventarioTRansformador"

const menuInformeInventarioInterventoria ={
    path: 'menu3inventario',
      component: () => import('@/views/informe/menu3inventario/index'), // Parent router-view
      name: 'menu_informe_menu3inventario',
      meta: { title: 'menu_informe_menu3inventario', icon: 'el-icon-files', roles: ['super', 'admin', 'pqrs', 'gerencia', 'ingeniero', 'supervisor', 'interventoria', 'admininventario'] 
    },
    children: [
        vistaInformeInventarioCanalizacion,
        vistaInformeInventarioControl,
        vistaInformeInventarioLuminaria,
        vistaInformeInventarioMedidor,
        vistaInformeInventarioPoste,
        vistaInformeInventarioRedes,
        vistaInformeInventarioTRansformador
    ]
}
export default menuInformeInventarioInterventoria