import menuAutorizacionFacturacion from "@/router/menuAutorizacionFacturacion"
import menuFacturacionCalculo from "@/router/menuFacturacionCalculo"
import menuCobranzaFacturacion from "@/router/menuCobranzaFacturacion"
import menuDesmonteFacturacion from "@/router/menuDesmonteFacturacion"
import menucierreReporteFacturacion from "@/router/menucierreReporteFacturacion"
import menuFormatoFacturacion from "@/router/menuFormatoFacturacion"
import menuGestionCuadrillaFacturacion from "@/router/menuGestionCuadrillaFacturacion"
import menuInformeReportesFacturacion from "@/router/menuInformeReportesFacturacion"
import menuObraFacturacion from "@/router/menuObraFacturacion"
import menuOrdenFacturacion from "@/router/menuOrdenFacturacion"
import menuReporteFacturacion from "@/router/menuOrdenFacturacion"
const perfilFacturacion = {
  path: '/facturacion',
  component: Layout,
  redirect: 'noredirect',
  name: 'Perfil/facturacion',
  single: false,
  meta: {
    title: 'Facturacion',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap', 'fact', 'facturacion']
  },
  children: [
    menuAutorizacionFacturacion,
    menuFacturacionCalculo,
    menuCobranzaFacturacion,
    menuDesmonteFacturacion,
    menucierreReporteFacturacion,
    menuFormatoFacturacion,
    menuGestionCuadrillaFacturacion,
    menuInformeReportesFacturacion,
    menuObraFacturacion,
    menuOrdenFacturacion,
    menuReporteFacturacion
  ]
}
export default perfilFacturacion