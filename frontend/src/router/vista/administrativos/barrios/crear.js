const vistaAdministrativosBarriosCrear = {
    path: 'barrio/editar/:id',
      component: _import('administracion/barrio/edit'),
      name: 'barrioedit',
      hidden: true,
      redirect: false,
      meta: {
        title: 'barrioedit',
        roles: ['super', 'admin', 'ingeniero', 'pqrs']
      }
}
export default vistaAdministrativosBarriosCrear