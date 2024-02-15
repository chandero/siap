const vistaAdministrativoContratista = {
    path: 'contratista',
      component: _import('administracion/contratista/index'),
      name: 'contratista',
      meta: {
        title: 'Contratista',
        roles: ['super', 'admin', 'ingeniero', 'supervisor']
      }
}
export default vistaAdministrativoContratista