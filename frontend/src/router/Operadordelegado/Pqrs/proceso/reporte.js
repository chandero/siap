import vistaReporteListaLuminarias from "@/router/vista/luminarias/listaluminarias"
import vistaReporteEditarLuminaria from "@/router/vista/vistaReporteEditarLuminaria"
import vistaReporteCrearLuminaria from "@/router/vistaReporteCrearLuminaria"
import vistaReporteControlCrear from "@/router/vistaReporteControlCrear"
import vistaReporteControlEditar from "@/router/vistaReporteControlEditar"
import vistaReporteControlListaLuminarias from "@/router/vistaReporteControlListaLuminarias"
import vistaReporteTransformadorCrear from "@/router/vistaReporteTransformadorCrear"
import vistaReporteTransformadorEditar from "@/router/vistaReporteTransformadorEditar"
import vistaReporteTransformadorLista from "@/router/vistaReporteTransformadorLista"




const menuReportePqrs = {
    path: 'menu1reporte',
    component: Modulo,
  redirect: 'noredirect',
  single: false,
  name: 'menu_proceso',
  meta: {
    title: 'menu_proceso',
    icon: 'el-icon-tickets',
    roles: ['super', 'admin', 'pqrs', 'supervisor']
  },
  children: [
    vistaReporteCrearLuminaria,
    vistaReporteEditarLuminaria,
    vistaReporteListaLuminarias,
    vistaReporteControlCrear,
    vistaReporteControlEditar,
    vistaReporteControlListaLuminarias,
    vistaReporteTransformadorCrear,
    vistaReporteTransformadorEditar,
    vistaReporteTransformadorLista
  ]
}
export default menuReportePqrs





