const vistaAdministrativosInventarioMedidorEditar = {
    path: 'medidor/editar/:id',
      component: _import('administracion/medidor/edit'),
      name: 'medidoredit',
      hidden: true,
      meta: {
        title: 'medidoredit',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
}
vistaAdministrativosInventarioMedidorEditar