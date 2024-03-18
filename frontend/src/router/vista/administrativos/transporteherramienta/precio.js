const vistaAdministrativoTransporteHerramientaPrecio = {
    path: 'price',
    component: () =>
      import('@/views/administracion/manoherramientaorden/menu02/index'),
    name: 'mano_herramienta_precio',
    meta: {
      title: 'mano_herramienta.precio',
      icon: 'el-icon-money',
      roles: ['super', 'admin', 'supervisor']
    }
}
export default vistaAdministrativoTransporteHerramientaPrecio