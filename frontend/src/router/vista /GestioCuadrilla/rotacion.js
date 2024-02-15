const vistaReporteRotacionCuadrilla = {
    path: 'menu8cuadrilla',
      component: () => import('@/views/proceso/menu8cuadrilla/index'), // Parent router-view
      name: 'menu_proceso_menu8cuadrilla.1',
      meta: {
        title: 'Rotacion de Cuadrilla',
        icon: 'el-icon-s-unfold',
        roles: [
          'super',
          'admin',
          'pqrs',
          'supervisor'
        ]
      }
}
export  default vistaReporteRotacionCuadrilla