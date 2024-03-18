const vistaAdministrativosInventarioMedidorCrear = {
    path: 'medidor/crear',
      component: _import('administracion/medidor/create'),
      name: 'medidorcreate',
      hidden: true,
      meta: {
        title: 'medidorcreate',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
}
vistaAdministrativosInventarioMedidorCrear