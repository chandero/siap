import vistaInventarioCanalizacionActiva from "@/router/vistaInventarioCanalizacionActiva"
import vistaInventariCanalizacionEDitar from "@/router/vistaInventariCanalizacionEDitar"
import vistaInventariCanalizacionCrear from "@/router/vistaInventariCanalizacionCrear"
import vistaInventarioCanalizacionEnBaja from "@/router/vistaInventarioCanalizacionEnBaja"

const menuInventarioCanalizacionPqrs = {
    path: 'menu3canalizacion',
      component: () => import('@/views/inventario/menu3canalizacion/index'), // Parent router-view
      name: 'menu_inventario_menu3canalizacion',
      meta: { title: 'menu_inventario_menu3canalizacion', icon: 'el-icon-cherry', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] 
    },
    children: [
        vistaInventarioCanalizacionActiva,
        vistaInventariCanalizacionEDitar,
        vistaInventariCanalizacionCrear,
        vistaInventarioCanalizacionEnBaja
    ]
}
export default menuInventarioCanalizacionPqrs