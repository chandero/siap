import vistaReporteObraPendiente from "@/router/vistaReporteObraPendiente";

const menuObraSupervisor = {
    path: 'menu3obra',
      component: () => import('@/views/proceso/menu3obra/index'), // Parent router-view
      name: 'menu_proceso_menu3obra',
      meta: {
        title: 'menu_proceso_menu3obra',
        icon: 'el-icon-picture-outline-round',
        roles: ['super', 'pqrs', 'ingeniero', 'supervisor']
      },
      children: [
        vistaReporteObraPendiente
      ]
    }
    export default menuObraSupervisor