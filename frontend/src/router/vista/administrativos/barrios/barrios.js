const vistaAdministrativosBarrios = {
  path: 'barrio/lista/:did?/:mid?',
  component: _import('administracion/barrio'),
  name: 'barrio',
  meta: {
    title: 'barrio',
    roles: ['super', 'admin', 'ingeniero', 'pqrs']
  }
}
export default vistaAdministrativosBarrios