import vistaInventarioTransformadorActivo from "@/router/vistaInventarioTransformadorActivo"
import vistaInventarioTransformadorCrear from "@/router/vistaInventarioTransformadorCrear"
import vistaInventarioTransformadorEditar from "@/router/vistaInventarioTransformadorEditar"
import vistaInventarioTransformadorEnBajo from "@/router/vistaInventarioTransformadorEnBajo"
const menuInventarioTransformadorAuditoria = {
    path: 'menu6transformador',
    component: () => import('@/views/inventario/menu6transformador/index'), // Parent router-view
    name: 'menu_inventario_menu6transformador',
    meta: { title: 'menu_inventario_menu6transformador', icon: 'el-icon-c-scale-to-original', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero']
    },
    children: [
        vistaInventarioTransformadorActivo,
        vistaInventarioTransformadorCrear,
        vistaInventarioTransformadorEditar,
        vistaInventarioTransformadorEnBajo
    ]
}
export default menuInventarioTransformadorAuditoria
