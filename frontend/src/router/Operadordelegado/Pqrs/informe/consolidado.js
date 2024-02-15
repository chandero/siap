import vistaInformeConsolidadoMaterialReportes from "@/router/vistaInformeConsolidadoMaterialReportes"
import vistaInformeConsolidadoEstadistica from "@/router/vistaInformeConsolidadoEstadistica"
import vistaInformeConsolidadoGeralOperaciones from "@/router/vistaInformeConsolidadoGeralOperaciones"
import vistaInformeConsolidadoMaterialUtilizadoPorBodega from "@/router/vistaInformeConsolidadoMaterialUtilizadoPorBodega"
import vistaInformeConsolidadoMateriaPorCuadrilla from "@/router/vistaInformeConsolidadoMateriaPorCuadrilla"
import VistaInformeConsolidadoMaterialUtilizado from "@/router/VistaInformeConsolidadoMaterialUtilizado"
import vistaiInformeConsolidadoMaterialUtilizadoPorTipo from "@/router/vistaiInformeConsolidadoMaterialUtilizadoPorTipo"
import vistaInformeConsolidadoMaterialUtilizadoCuadrilla from "@/router/vistaInformeConsolidadoMaterialUtilizadoCuadrilla"
import vistaInformeConsolidadoReporteSinFoto from "@/router/vistaSolicitudConsolidadoReporteSinFoto"
import vistaInformeConsolidadoResumenAforo from "@/router/vistaInformeConsolidadoResumenAforo"

const menuConsolidadoPqrs = {
    path: 'menu1consolidado',
      component: () => import('@/views/informe/menu1consolidado/index'), // Parent router-view
      name: 'menu_informe_menu1consolidado',
      meta: { title: 'menu_informe_menu1consolidado', icon: 'el-icon-document-remove',
      roles: ['super', 'pqrs', 'gerencia', 'ingeniero', 'supervisor', 'interventoria']
    },
    children: [
        vistaInformeConsolidadoMaterialReportes,
        vistaInformeConsolidadoEstadistica,
        vistaInformeConsolidadoGeralOperaciones,
        vistaInformeConsolidadoMaterialUtilizadoPorBodega,
        vistaInformeConsolidadoMateriaPorCuadrilla,
        VistaInformeConsolidadoMaterialUtilizado,
        vistaiInformeConsolidadoMaterialUtilizadoPorTipo,
        vistaInformeConsolidadoMaterialUtilizadoCuadrilla,
        vistaInformeConsolidadoReporteSinFoto,
        vistaInformeConsolidadoResumenAforo
    ]
}
export default menuConsolidadoPqrs
