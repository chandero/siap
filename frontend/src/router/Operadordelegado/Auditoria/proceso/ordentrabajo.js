import vistaReporteOrdenCrear from "@/router/vistaReporteOrdenCrear"
import vistaReporteOrdenEditar from "@/router/vistaReporteOrdenEditar"
import vistaReporteOrdenLista from "@/router/vistaReporteOrdenLista"
const menuOrdenAuditoria = {
    path: 'menu4-1interna',
          component: () => import('@/views/proceso/menu4orden/menu4-1interna'),
          name: 'menu_proceso_menu4orden_menu4-1interna',
          meta: {
            title: 'menu_proceso_menu4orden_menu4-1interna',
            icon: 'el-icon-truck',
            roles: ['super', 'admin', 'pqrs', 'ingeniero', 'supervisor']
          },
          children: [
            vistaReporteOrdenCrear,
            vistaReporteOrdenEditar,
            vistaReporteOrdenLista
          ]
        }
        export default menuOrdenAuditoria
