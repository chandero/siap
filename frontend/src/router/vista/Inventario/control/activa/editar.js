const vistaInventariControlEditar = {
    path: 'menu2-1activa/gestion/editar/:id', component: () => import('@/views/inventario/menu2control/menu2-1activa/gestion/edit/index'), 
    name: 'menu_inventario2.1.4', hidden: true, redirect: false, 
    meta: { title: 'controlgestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor']
    } 
}
export default vistaInventariControlEditar
