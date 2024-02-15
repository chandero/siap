import vistaInventarioLuminariaActiva from "@/router/vistaInventarioLuminariaActiva"
import vistaInventarioLuminariaCrearLUminaria from "@/router/vistaInventarioLuminariaCrearLUminaria"
import vistaInventarioLuminariaEditar from "@/router/vistaInventarioLuminariaEditar"
import vistaInventartarioLuminariaEnBaja from "@/router/vistaInventartarioLuminariaEnBaja"
import vistaInventartarioBuscarPorCodigo from "@/router/vistaInventartarioBuscarPorCodigo"
import vistaInventartarioBuscarPorMaterial from "@/router/vistaInventartarioBuscarPorMaterial"
import vistaInventarioHistorial from "@/router/vistaInventarioHistorial"
const menuInventarioLuminariaGerencia = {
    path: '/inven',
  component: Modulo,
  redirect: 'noredirect',
  name: 'menu_inventario',
  meta: {
    title: 'menu_inventario',
    icon: 'el-icon-files',
    roles: ['super', 'admin', 'pqrs', 'ingeniero', 'gerencia', 'supervisor']
  },
  children: [
    vistaInventarioLuminariaActiva,
    vistaInventarioLuminariaCrearLUminaria,
    vistaInventarioLuminariaEditar,
    vistaInventartarioLuminariaEnBaja,
    vistaInventartarioBuscarPorCodigo,
    vistaInventartarioBuscarPorMaterial,
    vistaInventarioHistorial
  ]
}
export default menuInventarioLuminariaGerencia