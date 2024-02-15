const vistaAdministrativosCuadrillaEditar = {
    path: 'cuadrilla/editar/:id',
    component: _import('administracion/cuadrilla/edit'),
    name: 'cuadrillaedit',
    hidden: true,
    redirect: false,
    meta: { title: 'cuadrillaedit', roles: ['super', 'admin', 'ingeniero', 'supervisor'] }
}
export default vistaAdministrativosCuadrillaEditar