const vistaReporteListaLuminarias = {
    path: 'menu1-1-1list',
    component: () =>
      import(
        '@/views/proceso/menu1reporte/menu1-1luminaria/menu1-1-1list'
      ),
    name: 'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-1list',
    hidden: true,
    meta: {
      title:
        'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-1list',
      roles: ['super', 'admin', 'pqrs']
    }
}
export default vistaReporteListaLuminarias