const vistaAdministrativosUsuarioCrear = {
    path: 'usuario/crear',
      component: _import('administracion/usuario/create'),
      name: 'usuariocreate',
      hidden: true,
      meta: { title: 'usuariocreate', roles: ['super', 'admin'] }
}
export default vistaAdministrativosUsuarioCrear