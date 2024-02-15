import menuActaObraSupervisor from "@/router/menuActaObraSupervisor"
import menuAutorizacionSupervisor from "@/router/menuAutorizacionSupervisor"
import menuConsolidadoSupervisor from "@/router/menuConsolidadoSupervisor"
import menuInformeInventarioSupervisor from "@/router/menuInformeInventarioSupervisor"
import menuGestionCuadrillaSupervisor from "@/router/menuGestionCuadrillaSupervisor"
import menuInformeReportesSupervisor from "@/router/menuInformeReportesSupervisor"
import menuObraSupervisor from "@/router/menuObraSupervisor"
import menuOrdenSupervisor from "@/router/menuOrdenSupervisor"
import menuSolicitudConsecutivoSupervisor from "@/router/menuSolicitudConsecutivoSupervisor"
import menuCartaSupervisor from "@/router/menuCartaSupervisor"
import menuInformeDetalladoSupervisor from "@/router/menuInformeDetalladoSupervisor"
import menuInformeVencidoSupervisor from "@/router/menuInformeInventarioSupervisor"
import menuInventarioCanalizacionSupervisor from "@/router/menuInventarioCanalizacionSupervisor"
import menuInventarioControlSupervisor from "@/router/menuInventarioControlSupervisor"
import menuInventarioLuminariaSupervisor from "@/router/menuInventarioLuminariaSupervisor"
import menuInventarioMedidorSupervisor from "@/router/menuInventarioMedidorSupervisor"
import menuInventarioPosteSupervisor from "@/router/menuInventarioPosteSupervisor"
import menuInventarioRedesSupervisor from "@/router/menuInformeReportesSupervisor"
import menuInventarioTransformadorSupervisor from "@/router/menuInventarioTransformadorSupervisor"
const perfilSupervisor = {
    path: '/supervisor',
    component: Layout,
    redirect: 'noredirect',
    name: 'Perfil/supervisor',
    single: false,
    meta: {
    title: 'Supervisor',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap', 'fact', 'facturacion', 'supervisor', 'pqrs']
  },
  children: [
    menuActaObraSupervisor, menuAutorizacionSupervisor, menuConsolidadoSupervisor, menuInformeInventarioSupervisor, menuGestionCuadrillaSupervisor,
    menuInformeInventarioSupervisor, menuInformeReportesSupervisor, menuObraSupervisor, menuOrdenSupervisor, menuSolicitudConsecutivoSupervisor,
    menuCartaSupervisor, menuInformeDetalladoSupervisor, menuInformeVencidoSupervisor, menuInventarioCanalizacionSupervisor,
    menuInventarioControlSupervisor, menuInventarioLuminariaSupervisor, menuInventarioMedidorSupervisor, menuInventarioPosteSupervisor,
    menuInformeReportesSupervisor, menuInventarioTransformadorSupervisor
  ]
}
export default perfilSupervisor