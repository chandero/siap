import menuCalculoAuditoria from "@/router/menuCalculoAuditoria"
import menuDetalladoAuditoria from "@/router/menuDetalladoAuditoria"
import menuFotograficoAuditoria from "@/router/menuFotograficoAuditoria"
import menuGraficaAuditoria from "@/router/menuGraficaAuditoria"
import menuInventarioAuditoria from "@/router/menuInventarioAuditoria"
import menuInventarioControlAuditoria from "@/router/menuInventarioControlAuditoria"
import menuInventarioLuminariaAuditoria from "@/router/menuInventarioMedidorAuditoria"
import menuInventarioMedidorAuditoria from "@/router/menuInventarioMedidorAuditoria"
import menuInventarioPosteAuditoria from "@/router/menuInventarioPosteAuditoria"
import menuInventarioRedesAuditoria from "@/router/menuInventarioRedesAuditoria"
import menuInventarioTransformadorAuditoria from "@/router/menuInventarioTransformadorAuditoria"
import menuInventarioCanalizacionAuditoria from "@/router/menuInventarioCanalizacionAuditoria"
import menuActaDesmonteAuditoria from "@/router/menuActaDesmonteAuditoria"
import menuCierreReporteAuditoria from "@/router/menuCierreReporteAuditoria"
import menuFormatoAuditoria from "@/router/menuFormatoAuditoria"
import menuGestionCuadrillaAuditoria from "@/router/menuGestionCuadrillaAuditoria"
import menuInformeReportesAuditoria from "@/router/menuInformeReportesAuditoria"
import menuObraAuditoria from "@/router/menuObraAuditoria"
import menuOrdenAuditoria from "@/router/menuObraAuditoria"
import menuReporteAuditoria from "@/router/menuReporteAuditoria"
const perfilAuditoria= {
  path: '/audiroria',
  component: Layout,
  redirect: 'noredirect',
  name: 'Perfil/auditoria',
  single: false,
  meta: {
    title: 'Auditoria',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap', 'fact', 'facturacion']
  },
  children: [
    menuCalculoAuditoria, menuDetalladoAuditoria, menuFotograficoAuditoria, menuGraficaAuditoria, menuInventarioAuditoria, menuInventarioControlAuditoria,
    menuInventarioLuminariaAuditoria, menuInventarioMedidorAuditoria, menuInventarioPosteAuditoria, menuInventarioRedesAuditoria, menuInventarioTransformadorAuditoria,
    menuInventarioCanalizacionAuditoria, menuActaDesmonteAuditoria, menuCierreReporteAuditoria, menuFormatoAuditoria, menuGestionCuadrillaAuditoria,
    menuInformeReportesAuditoria, menuOrdenAuditoria, menuObraAuditoria, menuReporteAuditoria
  ]
}
export default perfilAuditoria