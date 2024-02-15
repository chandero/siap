const vistaAdministrativoIpcPrecio = {
        path: 'price',
        component: () =>
          import('@/views/administracion/elemento/menu03/index'),
        name: 'elemento_precio',
        meta: {
          title: 'elemento.precio',
          icon: 'el-icon-money',
          roles: ['super', 'admin', 'supervisor']
        }
}
export default vistaAdministrativoIpcPrecio