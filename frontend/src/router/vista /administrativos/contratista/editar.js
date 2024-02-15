const vistaAdministrativoContratistaEditar = {
    path: 'contratista/editar/:id',
      component: _import('administracion/contratista/edit'),
      name: 'contratistaedit',
      hidden: true,
      redirect: false,
      meta: {
        title: 'Contratista Editar',
        roles: ['super', 'admin', 'ingeniero', 'supervisor']
      }
}
export default vistaAdministrativoContratistaEditar