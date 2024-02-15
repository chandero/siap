import vistaReporteCierreDirecto from "@/router/vistaReporteCierreDirecto"

const menucierreReportePqrs = {
    path: 'menu9cierre',
      component: () => import('@/views/proceso/menu9cierre/index'), // Parent router-view
      name: 'menu_proceso_menu9cierre',
      meta: {
        title: 'menu_proceso_menu9cierre',
        icon: 'el-icon-s-unfold',
        roles: [
          'super',
          '',
          'pqrs'
        ]
    },
    children: [
        vistaReporteCierreDirecto
    ]
}
export default menucierreReportePqrs