import vistaCalculoCarga from "@/router/vistaCalculoCarga"
import vistaActaObraLista from "@/router/vistaActaObraLista"
const menuFacturacionCalculo = {
    path: 'menu4calculo',
      component: () => import('@/views/informe/menu4calculo/index'), // Parent router-view
      name: 'menu_informe_menu4calculo',
      meta: { title: 'menu_informe_menu4calculo', icon: 'el-icon-data-analysis', roles: ['super', 'gerencia', 'ingeniero', 'interventoria', 'pqrs'] 
    },
    children: [
        vistaCalculoCarga,
        vistaActaObraLista
    ]
}
export default menuFacturacionCalculo