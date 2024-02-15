const vistaReporteEditarLuminaria = {
    path: 'menu1-1-3edit/:id',
    component: () =>
      import(
        '@/views/proceso/menu1reporte/menu1-1luminaria/menu1-1-3edit'
      ),
    name: 'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-3edit',
    hidden: true,
    meta: {
      title:
        'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-3edit',
      roles: ['super', 'admin', 'pqrs']
    }
}
export default vistaReporteEditarLuminaria