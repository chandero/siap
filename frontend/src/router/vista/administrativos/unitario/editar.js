const vistaAdministrativoUnitarioEditar = {
    path: 'unitario/editar/:id',
      component: _import('administracion/unitario/edit'),
      name: 'unitarioedit',
      hidden: true,
      redirect: false,
      meta: { title: 'unitarioedit', roles: ['super'] }
}
export default vistaAdministrativoUnitarioEditar