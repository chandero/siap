import vistaSolicitudCartaCrear from "@/router/vistaSolicitudCartaCrear"
import vistaSolicitudCartaEditar from "@/router/vistaSolicitudCartaEditar"
import vistaSolicitudCartaLista from "@/router/vistaSolicitudCartaLista"
const menuCartaInterventoria = {
    path: 'menu1carta',
      component: () => import('@/views/solicitud/menu1carta/index'), // Parent router-view
      name: 'menu_solicitud_menu1carta',
      meta: { title: 'menu_solicitud_menu1carta', icon: 'el-icon-message', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor'] 
    },
    children: [
        vistaSolicitudCartaCrear,
        vistaSolicitudCartaEditar,
        vistaSolicitudCartaLista
    ]
}
export default menuCartaInterventoria