const vistaAdministrativosUsuarioEditar = {
    path: 'usuario/editar/:id',
      component: _import('administracion/usuario/edit'),
      name: 'usuarioedit',
      hidden: true,
      meta: { title: 'usuarioedit', roles: ['super', 'admin'] }
}
export default vistaAdministrativosUsuarioEditar