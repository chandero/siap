import menuConsolidadoInterventoria from '@/router/menuConsolidadoInterventoria'
import menuDetalladoInterventoria from '@/router/menuDetalladoInterventoria'
import menuInformeInventarioInterventoria from '@/router/menuInformeInventarioInterventoria'
import menuInventarioCanalizacionInterventoria from '@/router/menuInformeInventarioInterventoria'
import menuInventarioControlInterventoria from '@/router/menuInventarioControlInterventoria'
import menuInventarioLuminariaInterventoria from '@/router/menuInventarioLuminariaInterventoria'
import menuInventarioPosteInterventoria from '@/router/menuInventarioPoste'
import menuInventarioRedesInterventoria from '@/router/menuInventarioRedesInterventoria'
import menuInventarioTransformadorInterventoria from '@/router/menuInventarioTransformadorInterventoria'
import menucierreReporteInterventoria from '@/router/menucierreReporteInterventoria'
import menuFormatoBlancoInterventoria from '@/router/menuFormatoBlancoInterventoria'
import menuGestionCuadrillaInterventoria from '@/router/menuGestionCuadrillaInterventoria'
import menuInformeReportesInterventoria from '@/router/menuInformeReportesInterventoria'
import menuObraInterventoria from '@/router/menuObraInterventoria'
import menuOrdenInterventoria from '@/router/menuOrdenInterventoria'
import menuReporteInterventoria from '@/router/menuOrdenInterventoria'
import menuSolicitudConsecutivoInterventoria from '@/router/menuSolicitudConsecutivoInterventoria'
import menuCartaInterventoria from '@/router/menuCartaInterventoria'
import menuInformeDetalladoInterventoria from '@/router/menuInformeDetalladoInterventoria'
import menuInformeVencidoInterventoria from '@/router/menuInformeVencidoInterventoria'
const perfilInterventoria = {
  path: '/interventoria',
  component: Layout,
  redirect: 'noredirect',
  name: 'Perfil/interventoria',
  single: false,
  meta: {
    title: 'Interventoria',
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
    menuConsolidadoInterventoria,
    menuDetalladoInterventoria,
    menuInformeInventarioInterventoria,
    menuInventarioCanalizacionInterventoria,
    menuInventarioControlInterventoria,
    menuInventarioLuminariaInterventoria,
    menuInventarioPosteInterventoria,
    menuInventarioRedesInterventoria,
    menuInventarioTransformadorInterventoria,
    menucierreReporteInterventoria,
    menuFormatoBlancoInterventoria,
    menuGestionCuadrillaInterventoria,
    menuInformeReportesInterventoria,
    menuObraInterventoria,
    menuOrdenInterventoria,
    menuReporteInterventoria,
    menuSolicitudConsecutivoInterventoria,
    menuCartaInterventoria,
    menuInformeDetalladoInterventoria,
    menuInformeVencidoInterventoria
  ]
}
export default perfilInterventoria
