const vistaAdministrativoElementoEditar = {
    path: 'tipoelemento/editar/:id',
      component: _import('administracion/tipoelemento/edit'),
      name: 'tipoelementoedit',
      hidden: true,
      redirect: false,
      meta: { title: 'tipoelementoedit', roles: ['super'] }
}
export default vistaAdministrativoElementoEditar