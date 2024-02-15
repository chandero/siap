import menuGerencia from "@/router/menuGerencia"
import menuCalculoGerencia from "@/router/menuCalculoGerencia"
import menuConsolidadoGerencia from "@/router/menuConsolidadoGerencia"
import menuDetalladoGerencia from "@/router/menuDetalladoGerencia"
import menuFotograficoGerencia from "@/router/menuFotograficoGerencia"
import menuGraficaGerencia from "@/router/menuGraficaGerencia"
import menuInformeInventarioGerencia from "@/router/menuInformeInventarioGerencia"
import menuInventarioCanalizacionGerencia from "@/router/menuInventarioCanalizacionGerencia"
import menuInventarioControlGerencia from "@/router/menuInventarioControlGerencia"
import menuInventarioLuminariaGerencia from "@/router/menuInventarioLuminariaGerencia"
import menuInventarioMedidorGerencia from "@/router/menuInventarioMedidorGerencia"
import menuInventarioPosteGerencia from "@/router/menuInventarioPosteGerencia"
import menuInventarioRedesGerencia from "@/router/menuInventarioRedesGerencia"
import menuInventarioTransformadorGerencia from "@/router/menuInventarioTransformadorGerencia"
const perfilGerencia = {
    path: '/Gerencia',
    component: Layout,
    redirect: 'noredirect',
    name: 'Perfil/Gereencia',
    single: false,
    meta: {
    title: 'Gerencia',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap']
  },
  children: [
    menuGerencia,
    menuCalculoGerencia,
    menuConsolidadoGerencia,
    menuDetalladoGerencia,
    menuFotograficoGerencia,
    menuGraficaGerencia,
    menuInformeInventarioGerencia,
    menuInventarioCanalizacionGerencia,
    menuInventarioControlGerencia,
    menuInventarioLuminariaGerencia,
    menuInventarioMedidorGerencia,
    menuInventarioPosteGerencia,
    menuInventarioRedesGerencia,
    menuInventarioTransformadorGerencia
  ]
}
export default perfilGerencia