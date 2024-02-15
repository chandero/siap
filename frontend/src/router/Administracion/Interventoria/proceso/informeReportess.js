import vistaReporteInformeCambios from "@/router/vistaReporteInformeCambios";
import vistaReporteInformeEjecutados from "@/router/vistaReporteInformeEjecutados";
import vistaReporteInformePendiente from "@/router/vistaReporteInformePendiente"

const menuInformeReportesInterventoria = {
    path: 'menu2pendiente',
      component: () => import('@/views/proceso/menu2pendiente/index'),
      name: 'menu_proceso_menu2pendiente',
      meta: {
        title: 'menu_proceso_menu2pendiente',
        icon: 'el-icon-s-unfold',
        roles: [
          'super',
          '',
          'pqrs',
          'gerencia',
          'ingeniero',
          'supervisor'
        ]
    },
    children: [
        vistaReporteInformeCambios,
        vistaReporteInformeEjecutados,
        vistaReporteInformePendiente
    ]
}
export default menuInformeReportesInterventoria