import vistaReporteObraCrear from "@/router/vistaReporteObraCrear"
import vistaReporteObraEditar from "@/router/vistaReporteObraEditar"
import vistaReporteObraLista from "@/router/vistaReporteObraLista";
import vistaReporteObraPendiente from "@/router/vistaReporteObraPendiente";

const menuObraFacturacion = {
    path: 'menu3obra',
      component: () => import('@/views/proceso/menu3obra/index'),
      name: 'menu_proceso_menu3obra',
      meta: {
        title: 'menu_proceso_menu3obra',
        icon: 'el-icon-picture-outline-round',
        roles: ['super', 'pqrs', 'ingeniero', 'supervisor']
      },
      children: [
        vistaReporteObraCrear,
        vistaReporteObraEditar,
        vistaReporteObraLista,
        vistaReporteObraPendiente
      ]
    }
    export default menuObraFacturacion