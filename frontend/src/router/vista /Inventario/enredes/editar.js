const vistaInventarioRedesEditar = {
    path: 'menu5-1activa/gestion/editar/:id', component: () => import('@/views/inventario/menu5redes/menu5-1activa/gestion/edit/index'),
    name: 'menu_inventario5.1.4', hidden: true, redirect: false, meta: { title: 'redesgestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] }
}
export default vistaInventarioRedesEditar