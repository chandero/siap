import vistaAutorizacionAbrirReporte from "@/router/vistaAutorizacionAbrirReporte"
import vistaAutorizacionCargarReporte from "@/router/vistaAutorizacionCargarReporte"
import vistaAutorizacionCrearReporte from "@/router/vistaAutorizacionCrearReporte"
import vistaAutorizacionRecuperarReporte from "@/router/vistaAutorizacionRecuperarReporte"
const menuAutorizacionSupervisor = {
    path: 'autorizacion',
  component: Modulo,
  redirect: 'noredirect',
  single: false,
  name: 'autorizacion',
  meta: { title: 'Autorizacion', icon: 'el-icon-search', roles: ['super', 'admin', 'supervisor', 'fact']
    },
    children: [
        vistaAutorizacionAbrirReporte,
        vistaAutorizacionCargarReporte,
        vistaAutorizacionCrearReporte,
        vistaAutorizacionRecuperarReporte
    ]
}
export default menuAutorizacionSupervisor