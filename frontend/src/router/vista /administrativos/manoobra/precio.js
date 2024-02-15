const vistaAdministrativoManoObraPrecio = {
    path: 'price',
          component: () =>
            import('@/views/administracion/manoobraorden/menu02/index'),
          name: 'mano_obra_precio',
          meta: {
            title: 'mano_obra.precio',
            icon: 'el-icon-money',
            roles: ['super', 'admin', 'supervisor']
          }
}
export default vistaAdministrativoManoObraPrecio