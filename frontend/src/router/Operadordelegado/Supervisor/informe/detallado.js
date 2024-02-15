import vistaDetalladoActualizacion from "@/router/vistaDetalladoActualizacion"
import vistaDetalladoCambioMedida from "@/router/vistaDetalladoCambioMedida"
import vistaDetalladoCuadrillaObra from "@/router/vistaDetalladoCuadrillaObra"
import vistaDetalladoExpancion from "@/router/vistaDetalladoExpancion"
import vistaDetalladoLuminariasReporte from "@/router/vistaDetalladoLuminariasReporte"
import vistaDetalladoMaterial from "@/router/vistaDetalladoMaterial"
import vistaDetalladoModernizacion from "@/router/vistaDetalladoModernizacion"
import vistaDetalladoPorBarrio from "@/router/vistaDetalladoPorBarrio"
import vistaDetalladoRepetido from "@/router/vistaDetalladoRepetido"
import vistaDetalladoReposicion from "@/router/vistaDetalladoReposicion"
import vistaDetalladoRepotenciacion from "@/router/vistaDetalladoRepotenciacion"
import vistaDetalladoRetiro from "@/router/vistaDetalladoRetiro"
import vistaDetalladoRetiroVsReubicacion from "@/router/vistaDetalladoRetiroVsReubicacion"
import vistaDetalladoReubicacion from "@/router/vistaDetalladoReubicacion"
const menuDetalladoSupervisor = {
    path: 'menu2detallado',
      component: () => import('@/views/informe/menu2detallado/index'), // Parent router-view
      name: 'menu_informe_menu2detallado',
      meta: { title: 'menu_informe_menu2detallado', icon: 'el-icon-document', roles: ['super', 'pqrs', 'gerencia', 'ingeniero', 'supervisor', 'interventoria', 'admin']
    },
    children: [
        vistaDetalladoActualizacion,
        vistaDetalladoCambioMedida,
        vistaDetalladoCuadrillaObra,
        vistaDetalladoExpancion,
        vistaDetalladoLuminariasReporte,
        vistaDetalladoMaterial,
        vistaDetalladoModernizacion,
        vistaDetalladoPorBarrio,
        vistaDetalladoRepetido,
        vistaDetalladoReposicion,
        vistaDetalladoRepotenciacion,
        vistaDetalladoRetiro,
        vistaDetalladoRetiroVsReubicacion,
        vistaDetalladoReubicacion
    ]
}
export default menuDetalladoSupervisor