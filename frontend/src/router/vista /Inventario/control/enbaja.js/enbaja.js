const vistaInventarioControlEnBaja = {
    path: 'menu2-2enbaja',
    component: () => import('@/views/inventario/menu2control/menu2-2enbaja'),
    name: 'menu_inventario2.1.3',
    meta: { title: 'menu_inventario_menu2control_menu2-2enbaja', icon: 'el-icon-sunset', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] }
}
export default vistaInventarioControlEnBaja

/*revisar este modulo, no arroja la busqueda en ningun filtro */