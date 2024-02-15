import vistaGraficaOperativa from "@/router/vistaGraficaOperativa"
const menuGraficaInventario = {
    path: 'menu5grafica',
      component: () => import('@/views/informe/menu5grafica/index'), // Parent router-view
      name: 'menu_informe_menu5grafica',
      meta: { title: 'menu_informe_menu5grafica', icon: 'el-icon-pie-chart', roles: ['super', 'gerencia', 'ingeniero', 'interventoria', 'pqrs'] 
    },
    children: [
        vistaGraficaOperativa
    ]
}
export default menuGraficaInventario