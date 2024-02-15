import menuCalculoInventario from '@/router/menuCalculoInventario'
import menuConsolidadoInventario from '@/router/menuConsolidadoInventario'
import menuDetalladoInventario from '@/router/menuDetalladoInventario'
import menuFotograficoInventario from '@/router/menuFotograficoInventario'
import menuGraficaInventario from '@/router/menuGraficaInventario'
import menuInformeInventario from '@/router/menuInformeInventario'
const perfilInventario = {
  path: '/inventario',
  component: Layout,
  redirect: 'noredirect',
  name: 'Perfil/inventario',
  single: false,
  meta: {
    title: 'Inventario',
    icon: 'audit',
    roles: [
      'super',
      'admin',
      'administradorsap',
      'operadorsap',
      'fact',
      'facturacion',
      'supervisor',
      'pqrs'
    ]
  },
  children: [
    menuCalculoInventario,
    menuConsolidadoInventario,
    menuDetalladoInventario,
    menuFotograficoInventario,
    menuGraficaInventario,
    menuInformeInventario
  ]
}
export default perfilInventario
