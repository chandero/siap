const vistaAdministrativoIpc = {
    path: 'ipp',
          component: () =>
            import('@/views/administracion/elemento/menu04/index'),
          name: 'ucap_ipp',
          meta: {
            title: 'ipp.lista',
            icon: 'el-icon-notebook-2',
            roles: ['super', 'admin', 'supervisor']
          }
}
export default vistaAdministrativoIpc