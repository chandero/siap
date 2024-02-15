const vistaAdministrativoContratistaCrear = {
    path: 'contratista/crear',
    component: _import('administracion/contratista/create'),
    name: 'contratistacreate',
    hidden: true,
    meta: {
      title: 'Contratista Crear',
      roles: ['super', 'admin', 'ingeniero', 'supervisor']
    }
}
export default vistaAdministrativoContratistaCrear