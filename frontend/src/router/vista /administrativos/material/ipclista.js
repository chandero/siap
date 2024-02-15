const vistaAdministrativoIpcLista = {
    path: 'list',
          component: () =>
            import('@/views/administracion/elemento/menu00/index'),
          name: 'elemento_lista',
          meta: {
            title: 'elemento.lista',
            icon: 'el-icon-info',
            roles: [
              'super',
              'admin',
              'pqrs',
              'gerencia',
              'ingeniero',
              'supervisor'
            ]
          }
}
export default vistaAdministrativoIpcLista