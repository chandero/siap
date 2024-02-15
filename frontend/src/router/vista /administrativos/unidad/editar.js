const vistaAdministrativosUnidadEditar = {
    path: 'unidad/editar/:id',
      component: _import('administracion/unidad/edit'),
      name: 'unidadedit',
      hidden: true,
      redirect: false,
      meta: { title: 'unidadedit', roles: ['super'] }
}
export default vistaAdministrativosUnidadEditar