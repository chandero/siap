import vistaFacturacionCobranzaActa from "@/router/vistaFacturacionCobranzaActa"
import vistaFacturacionCobranzaFactura from "@/router/vistaFacturacionCobranzaFactura"
import vistaFacturacionCobranzaLista from "@/router/vistaFacturacionCobranzaLista"
const menuCobranzaFacturacion = {
    path: 'menu4orden',
    component: () => import('@/views/proceso/menu4orden/index'),
    name: 'menu4-3cobro',
    meta: { title: 'Opciones', icon: 'el-icon-search', roles: ['super', 'admin', 'supervisor', 'fact'] }
}
export default menuCobranzaFacturacion