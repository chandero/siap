const vistaAdministrativoTransporteHerramientaLista = {
    path: 'list',
          component: () =>
            import('@/views/administracion/manoherramientaorden/menu01/index'),
          name: 'mano_herramienta_lista',
          meta: {
            title: 'mano_herramienta.lista',
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
export default vistaAdministrativoTransporteHerramientaLista