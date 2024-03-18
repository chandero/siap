const vistaReporteCrearLuminaria = {
    path: 'menu1-1-2create/:tireuc_id',
    component: () =>
      import(
        '@/views/proceso/menu1reporte/menu1-1luminaria/menu1-1-2create'
      ),
    name:
      'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-2create',
    hidden: true,
    meta: {
      title:
        'menu_proceso_menu1reporte_menu1-1luminaria_menu1-1-2create',
      roles: ['super', 'admin', 'pqrs']
    }
}
export default vistaReporteCrearLuminaria