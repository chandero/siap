import vistaReporteDesmonte from "@/router/vistaReporteDesmonte"
const menuActaDesmonteAuditoria = {
    path: 'menu7actadesmonte',
    component: () => import('@/views/proceso/menu7actadesmonte/index'), // Parent router-view
    name: 'menu_proceso_menu7actadesmonte',
    meta: {
      title: 'menu_proceso_menu7actadesmonte',
      icon: 'el-icon-refresh-right',
      roles: ['super', 'pqrs']
    },
    children: [
        vistaReporteDesmonte
    ] 
}
export default menuActaDesmonteAuditoria