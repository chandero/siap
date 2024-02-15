import menuAdministrativosPqrs from "@/router/menuAdministrativosPqrs"
import menuCalculoPqrs from "@/router/menuCalculoPqrs"
import menuConsolidadoPqrs from "@/router/menuConsolidadoPqrs"
import menuDetalladoPqrs from "@/router/menuDetalladoPqrs"
import menuFotograficoPqrs from "@/router/menuFotograficoPqrs"
import menuInformeInventarioPqrs from "@/router/menuInformeInventarioPqrs"
import menuInventarioCanalizacionPqrs from "@/router/menuInventarioCanalizacionPqrs"
import menuInventarioControlPqrs from "@/router/menuInventarioControlPqrs"
import menuInventarioLuminariaPqrs from "@/router/menuInventarioLuminariaPqrs"
import menuInventarioMedidorPqrs from "@/router/menuInventarioMedidorPqrs"
import menuInventarioPostePqrs from "@/router/menuInventarioPostePqrs"
import menuInventarioRedesPqrs from "@/router/menuInventarioRedesPqrs"
import menuInventarioTransformadorPqrs from "@/router/menuInventarioTransformadorPqrs"
import menucierreReportePqrs from "@/router/menucierreReportePqrs"
import menuformatoPqrs from "@/router/menuformatoPqrs"
import menuGestionCuadrillaPqrs from "@/router/menuGestionCuadrillaPqrs"
import menuInformeReportesPqrs from "@/router/menuInformeReportesPqrs"
import menuObraPqrs from "@/router/menuObraPqrs"
import menuOrdenPqrs from "@/router/menuOrdenPqrs"
import menuReportePqrs from "@/router/menuReportePqrs"
import menuCartaPqrs from "@/router/menuCartaPqrs"
import menuInformeDetalladoPqrs from "@/router/menuInformeDetalladoPqrs"
const perfilPqrs = {
    path: '/pqrs',
    component: Layout,
    redirect: 'noredirect',
    name: 'Perfil/pqrs',
    single: false,
    meta: {
    title: 'Pqrs',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap', 'fact', 'facturacion', 'supervisor', 'pqrs']
  },
  children: [
    menuAdministrativosPqrs, menuCalculoPqrs, menuConsolidadoPqrs, menuDetalladoPqrs, menuFotograficoPqrs, menuInformeInventarioPqrs, menuInventarioCanalizacionPqrs,
    menuInventarioControlPqrs, menuInventarioLuminariaPqrs, menuInventarioMedidorPqrs, menuInventarioPostePqrs, menuInventarioRedesPqrs, menuInventarioTransformadorPqrs,
    menucierreReportePqrs, menuformatoPqrs, menuGestionCuadrillaPqrs, menuInformeReportesPqrs, menuObraPqrs, menuOrdenPqrs, menuReportePqrs, menuCartaPqrs, menuInformeDetalladoPqrs
  ]
}
export default perfilPqrs