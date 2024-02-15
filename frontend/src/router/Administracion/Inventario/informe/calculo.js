import vistaCalculoDisponibilidad from "@/router/vistaCalculoDisponibilidad"
import vistaCalculoEficiencia from "@/router/vistaCalculoEficiencia"
import vistaCalculoCarga from "@/router/vistaCalculoCarga"
import vistaCalculoIndisponibilidad from "@/router/vistaCalculoIndisponibilidad"
import vistaCalculoUnidadConstructiva from "@/router/vistaCalculoUnidadConstructiva"
const menuCalculoInventario = {
    path: 'menu4calculo',
      component: () => import('@/views/informe/menu4calculo/index'), // Parent router-view
      name: 'menu_informe_menu4calculo',
      meta: { title: 'menu_informe_menu4calculo', icon: 'el-icon-data-analysis', roles: ['super', 'gerencia', 'ingeniero', 'interventoria', 'pqrs'] 
    },
    children: [
        vistaCalculoDisponibilidad,
        vistaCalculoEficiencia,
        vistaCalculoCarga,
        vistaCalculoIndisponibilidad,
        vistaCalculoUnidadConstructiva
    ]
}
export default menuCalculoInventario