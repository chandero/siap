const vistaAdministrativosUrbanizadoraLista = {
    path: 'urbanizadora/editar/:id',
      component: _import('administracion/urbanizadora/edit'),
      name: 'urbaedit',
      hidden: true,
      meta: {
        title: 'urbaedit',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
}
export default vistaAdministrativosUrbanizadoraLista