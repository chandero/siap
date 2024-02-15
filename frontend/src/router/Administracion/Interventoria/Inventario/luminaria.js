import vistaInventarioLuminariaActiva from "@/router/vistaInventarioLuminariaActiva"
import vistaInventarioLuminariaCrearLUminaria from "@/router/vistaInventarioLuminariaCrearLUminaria"
import vistaInventarioLuminariaEditar from "@/router/vistaInventarioLuminariaEditar"
import vistaInventartarioLuminariaEnBaja from "@/router/vistaInventartarioLuminariaEnBaja"
import vistaInventartarioBuscarPorCodigo from "@/router/vistaInventartarioBuscarPorCodigo"
import vistaInventartarioBuscarPorMaterial from "@/router/vistaInventartarioBuscarPorMaterial"
import vistaInventarioHistorial from "@/router/vistaInventarioHistorial"

const menuInventarioLuminariaInterventoria = {
  path: 'menu1luminaria',
  component: () => import('@/views/inventario/menu1luminaria/index'), // Parent router-view
  name: 'menu_inventario_menu1luminaria',
  meta: { title: 'menu_inventario_menu1luminaria', icon: 'el-icon-grape', roles: ['super', 'admin', 'pqrs', 'gerencia', 'supervisor', 'ingeniero'] },
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
export default menuInventarioLuminariaInterventoria