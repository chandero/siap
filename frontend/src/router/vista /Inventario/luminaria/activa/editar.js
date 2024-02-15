const vistaInventarioLuminariaEditar = {
     path: 'menu1-1activa/gestion/editar/:id', 
     component: () => import('@/views/inventario/menu1luminaria/menu1-1activa/gestion/edit/index'), 
     name: 'menu_inventario1.1.3', hidden: true, redirect: false, meta: { title: 'gestionedit', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] 
    } 
}
export default vistaInventarioLuminariaEditar