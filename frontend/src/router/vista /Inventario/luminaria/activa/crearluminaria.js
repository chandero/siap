const vistaInventarioLuminariaCrearLUminaria = {
    path: 'menu1-1activa/gestion/crear',
    component: () => import('@/views/inventario/menu1luminaria/menu1-1activa/gestion/create/index'),
    name: 'gestioncreate', hidden: true, meta: { title: 'gestioncreate', roles: ['super', 'admin', 'ingeniero', 'pqrs', 'supervisor'] } } 
export default vistaInventarioLuminariaCrearLUminaria