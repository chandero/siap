const vistaAdministrativosCaracteristicaEditar = {
    path: 'caracteristica/editar/:id',
      component: _import('administracion/caracteristica/edit'),
      name: 'caracteristicaedit',
      hidden: true,
      redirect: false,
      meta: { title: 'caracteristicaedit', roles: ['super'] }
}
export default vistaAdministrativosCaracteristicaEditar