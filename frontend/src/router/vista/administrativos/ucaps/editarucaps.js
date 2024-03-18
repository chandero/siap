const vistaAdministrativosUcapsEditar = {
    path: 'ucap/editar/:id',
      component: _import('administracion/ucap/edit'),
      name: 'ucapedit',
      hidden: true,
      redirect: false,
      meta: { title: 'ucapedit', roles: ['super'] }
}
export default vistaAdministrativosUcapsEditar