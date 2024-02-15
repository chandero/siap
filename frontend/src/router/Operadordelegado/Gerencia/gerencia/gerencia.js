import vistaGerenciaGrafica from "@/router/vistaGerenciaGrafica"
import vistaGerenciaPorUso from "@/router/vistaGerenciaPorUso"
const menuGerencia = {
    path: '/gerencia',
  component: Modulo,
  redirect: 'noredirect',
  name: 'gerencia',
  single: false,
  meta: {
    title: 'gerencia',
    icon: 'el-icon-s-management',
    roles: ['super', 'admin', 'ingeniero', 'gerencia']
  },
  children: [
    vistaGerenciaGrafica,
    vistaGerenciaPorUso
  ]
}
export default menuGerencia