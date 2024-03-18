const vistaGraficaOperativa = {
    path: 'menu5grafica',
          component: () => import('@/views/informe/menu5grafica/menu5-1operativa'),
          name: 'menu_consolidado5.1.2',
          meta: { title: 'menu_informe_menu5grafica_menu5-1operativa', icon: 'el-icon-info', roles: ['super', 'admin', 'pqrs', 'gerencia', 'ingeniero', 'supervisor', 'interventoria'] }
}
export default vistaGraficaOperativa