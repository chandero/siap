import vistaActaObraCrear from "@/router/vistaActaObraCrear"
import vistaActaObraEditar from "@/router/vistaActaObraEditar"
import vistaActaObraItem from "@/router/vistaActaObraItem"
import vistaActaObraLista from "@/router/vistaActaObraLista"
import vistaActaObraLista from "../../../vista /acta-obra/lista"
const menuActaObraSupervisor = {
    path: 'ao',
  component: Modulo,
  redirect: 'noredirect',
  single: false,
  name: 'menu_acta_obra',
  meta: {
    title: 'menu_acta_obra',
    icon: 'el-icon-truck',
    roles: ['super', 'auxiliar', 'ingeniero']
  },
  children: [
    vistaActaObraCrear,
    vistaActaObraEditar,
    vistaActaObraItem,
    vistaActaObraLista
  ]
}
export default menuActaObraSupervisor