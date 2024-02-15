import menuInventarioCanalizacion from "@/router/menuInventarioCanalizacion"
import menuInventarioControl from "@/router/menuInventarioControl"
import menuInventarioLuminaria from "@/router/menuInventarioLuminaria"
import menuInventarioMedidor from "@/router/menuInventarioMedidor"
import menuInventarioPoste from "@/router/menuInventarioPoste"
import menuInventarioRedes from "@/router/menuInventarioRedes"
import menuInventarioTransformador from "@/router/menuInventarioTransformador"
const perfilInventario = {
    path: '/Inventario',
    component: Layout,
    redirect: 'noredirect',
    name: 'Perfil/Inventario',
    single: false,
    meta: {
    title: 'Inventario',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap', 'fact', 'facturacion', 'supervisor', 'pqrs']
  },
  children: [
    menuInventarioCanalizacion,
    menuInventarioControl,
    menuInventarioLuminaria,
    menuInventarioMedidor,
    menuInventarioPoste,
    menuInventarioRedes,
    menuInventarioTransformador
  ]
}
export default perfilInventario