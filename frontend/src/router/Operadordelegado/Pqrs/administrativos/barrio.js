import vistaAdministrativosBarrios from "@/router/vistaAdministrativosBarrios"
import vistaAdministrativosBarriosCrear from "@/router/vistaAdministrativosBarriosCrear"
import vistaAdministrativosBarriosEditar from "@/router/vistaAdministrativosBarriosEditar"
const menuAdministrativosPqrs = {
    path: '/administracion',
  component: Modulo,
  redirect: 'noredirect',
  name: 'administracion',
  single: false,
  meta: {
    title: 'administracion',
    icon: 'el-icon-setting',
    roles: ['super', 'admin', 'ingeniero', 'pqrs']
  },
  children: [
    vistaAdministrativosBarrios,
    vistaAdministrativosBarriosCrear,
    vistaAdministrativosBarriosEditar
  ]
}
export default menuAdministrativosPqrs