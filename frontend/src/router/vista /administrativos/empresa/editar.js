const vistaAdministrativosEmpresaEditar = {
    path: 'empresa/editar/:id',
    component: _import('administracion/empresa/edit'),
    name: 'empresaedit',
    hidden: true,
    meta: { title: 'empresaedit', roles: ['super'] }
}
export default vistaAdministrativosEmpresaEditar