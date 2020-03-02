import Vue from 'vue'
import Router from 'vue-router'
import Aap from '../views/inventario/gestion/create.vue'
const _import = require('./_import_' + process.env.NODE_ENV)
// in development-env not use lazy-loading, because lazy-loading too many pages will cause webpack hot update too slow. so only in production use lazy-loading;
// detail: https://panjiachen.github.io/vue-element-admin-site/#/lazy-loading

Vue.use(Router)

/* Layout */
import Layout from '../views/layout/Layout'

/** note: submenu only apppear when children.length>=1
*   detail see  https://panjiachen.github.io/vue-element-admin-site/#/router-and-nav?id=sidebar
**/

/**
* hidden: true                   if `hidden:true` will not show in the sidebar(default is false)
* alwaysShow: true               if set true, will always show the root menu, whatever its child routes length
*                                if not set alwaysShow, only more than one route under the children
*                                it will becomes nested mode, otherwise not show the root menu
* redirect: noredirect           if `redirect:noredirect` will no redirct in the breadcrumb
* name:'router-name'             the name is used by <keep-alive> (must set!!!)
* meta : {
    roles: ['super', 'admin','editor']     will control the page roles (you can set multiple roles)
    title: 'title'               the name show in submenu and breadcrumb (recommend set)
    icon: 'svg-name'             the icon show in the sidebar,
    noCache: true                if true ,the page will no be cached(default is false)
  }
**/
export const constantRouterMap = [
  { path: '/login', component: _import('login/index'), hidden: true },
  { path: '/recovery', component: _import('recovery/index'), hidden: true },
  { path: '/r/:e', component: _import('recovery/changepass'), hidden: true },
  { path: '/empresa', component: _import('empresa/index'), hidden: true },
  { path: '/authredirect', component: _import('login/authredirect'), hidden: true },
  { path: '/404', component: _import('errorPage/404'), hidden: true },
  { path: '/401', component: _import('errorPage/401'), hidden: true },
  {
    path: '',
    component: Layout,
    redirect: 'dashboard',
    single: true,
    children: [{
      path: 'dashboard',
      component: _import('dashboard/index'),
      name: 'dashboard',
      meta: { title: 'dashboard', icon: 'dashboard', noCache: true }
    }]
  }/*,
  {
    path: '/documentation',
    component: Layout,
    redirect: '/documentation/index',
    single: true,
    children: [{
      path: 'index',
      component: _import('documentation/index'),
      name: 'documentation',
      meta: { title: 'documentation', icon: 'documentation', noCache: true }
    }]
  }*/
]

export default new Router({
  scrollBehavior: () => ({ y: 0 }),
  routes: constantRouterMap
})

export const asyncRouterMap = [
  {
    path: '/procesos',
    component: Layout,
    redirect: 'noredirect',
    name: 'procesos',
    single: false,
    meta: {
      title: 'procesos',
      icon: 'table',
      roles: ['super', 'gerencia', 'admin', 'ingeniero', 'supervisor', 'auxiliar', 'almacenista', 'liniero', 'reporte']
    },
    children: [
      { path: 'reporte', component: _import('procesos/reporte2'), name: 'reporte', meta: { title: 'reporte', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      {
        path: 'reporte/editar/:id',
        components: {
          default: _import('procesos/reporte/edit'),
          aap_create: Aap
        },
        name: 'reporteedit',
        hidden: true,
        redirect: false,
        meta: { title: 'reporteedit', roles: ['super', 'admin', 'auxiliar'] }
      },
      {
        path: 'reporte/editartags/:id',
        components: {
          default: _import('procesos/reporte/edittags'),
          aap_create: Aap
        },
        name: 'reporteedittags',
        hidden: true,
        redirect: false,
        meta: { title: 'reporteedittags', roles: ['super', 'admin', 'auxiliar'] }
      },
      { path: 'reporte/crear', component: _import('procesos/reporte/create'), name: 'reportecreate', hidden: true, meta: { title: 'reportecreate', roles: ['super', 'admin', 'auxiliar'] }},
      { path: 'reporte/relacion', component: _import('procesos/reporte/relacion'), name: 'reporterelacion', meta: { title: 'reporterelacion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'reporte'] }},
      { path: 'control', component: _import('procesos/control2'), name: 'controlreporte', meta: { title: 'controlreporte', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      {
        path: 'control/editar/:id',
        components: {
          default: _import('procesos/control/edit'),
          aap_create: Aap
        },
        name: 'controlreporteedit',
        hidden: true,
        redirect: false,
        meta: { title: 'controlreporteedit', roles: ['super', 'admin', 'auxiliar'] }
      },
      {
        path: 'control/editartags/:id',
        components: {
          default: _import('procesos/control/edittags'),
          aap_create: Aap
        },
        name: 'controlreporteedittags',
        hidden: true,
        redirect: false,
        meta: { title: 'controlreporteedittags', roles: ['super', 'admin', 'auxiliar'] }
      },
      { path: 'control/crear', component: _import('procesos/control/create'), name: 'controlreportecreate', hidden: true, meta: { title: 'controlreportecreate', roles: ['super', 'admin', 'auxiliar'] }},
      { path: 'control/relacion', component: _import('procesos/control/relacion'), name: 'controlreporterelacion', meta: { title: 'controlreporterelacion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'reporte'] }},
      { path: 'obra', component: _import('procesos/obra2'), name: 'obra', meta: { title: 'obra', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      {
        path: 'obra/editar/:id',
        components: {
          default: _import('procesos/obra/edit'),
          aap_create: Aap
        },
        name: 'obraedit',
        hidden: true,
        redirect: false,
        meta: { title: 'obraedit', roles: ['super', 'admin', 'auxiliar'] }
      },
      { path: 'obra/crear', component: _import('procesos/obra/create'), name: 'obracreate', hidden: true, meta: { title: 'obracreate', roles: ['super', 'admin', 'auxiliar'] }},
      { path: 'obra/relacion', component: _import('procesos/obra/relacion'), name: 'obrarelacion', meta: { title: 'obrarelacion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'reporte'] }},
      { path: 'buscarpormaterial', component: _import('procesos/buscarpormaterial'), name: 'buscarpormaterial', meta: { title: 'buscarpormaterial', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia', 'almacenista'] }},
      {
        path: 'subreporte/editar/:id',
        components: {
          default: _import('procesos/subreporte/edit'),
          aap_create: Aap
        },
        name: 'subreporteedit',
        hidden: true,
        redirect: false,
        meta: { title: 'subreporteedit', roles: ['super', 'admin', 'auxiliar'] }
      },
      { path: 'subreporte/crear', component: _import('procesos/subreporte/create'), name: 'subreportecreate', hidden: true, meta: { title: 'reportecreate', roles: ['super', 'admin', 'auxiliar'] }},
      { path: 'reporte/formato', component: _import('procesos/reporte/formato'), name: 'reporteformato', meta: { title: 'reporteformato', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'reporte/reparar', component: _import('procesos/reporte/reparar'), name: 'reportereparar', meta: { title: 'reportereparar', roles: ['super'] }},
      { path: 'ordentrabajo', component: _import('procesos/ordentrabajo'), name: 'ordentrabajo', hidden: false, meta: { title: 'ordentrabajo', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'ordentrabajo/editar/:id', component: _import('procesos/ordentrabajo/edit'), name: 'ordentrabajoedit', hidden: true, redirect: false, meta: { title: 'ordentrabajoedit', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'ordentrabajo/crear', component: _import('procesos/ordentrabajo/create'), name: 'ordentrabajocreate', hidden: true, meta: { title: 'ordentrabajocreate', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'muob', component: _import('procesos/municipioobra'), name: 'municipioobra', meta: { title: 'municipioobra', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'muob/editar/:id', components: { default: _import('procesos/municipioobra/edit'), aap_create: Aap }, name: 'muobedit', hidden: true, redirect: false, meta: { title: 'muobedit', roles: ['super', 'admin', 'auxiliar'] }},
      { path: 'muob/crear', component: _import('procesos/municipioobra/create'), name: 'muobcreate', hidden: true, meta: { title: 'muobcreate', roles: ['super', 'admin', 'auxiliar'] }},
      { path: 'muob/info/:consecutivo', components: { default: _import('procesos/municipioobra/rdetalladomat'), aap_create: Aap }, name: 'muobinfo', hidden: true, redirect: false, meta: { title: 'muobinfo', roles: ['super', 'admin', 'auxiliar', 'ingeniero'] }},
      { path: 'entregamaterial', component: _import('procesos/entregamaterial'), name: 'entregamaterial', hidden: true, meta: { title: 'entregamaterial', roles: ['super', 'admin', 'ingeniero', 'almacenista'] }},
      { path: 'recepcionmaterial', component: _import('procesos/recepcionmaterial'), name: 'recepcionmaterial', hidden: true, meta: { title: 'recepcionmaterial', roles: ['super', 'admin', 'ingeniero', 'almacenista'] }},
      { path: 'vencido', component: _import('procesos/vencido'), name: 'vencido', hidden: true, meta: { title: 'vencido', roles: ['super', 'gerencia', 'admin', 'ingeniero', 'auxiliar', 'reporte'] }}
    ]
  },
  {
    path: '/solicitud',
    component: Layout,
    redirect: 'noredirect',
    name: 'solicitud',
    single: false,
    meta: {
      title: 'solicitud',
      icon: 'message',
      roles: ['super', 'admin', 'auxiliar', 'supervisor', 'reporte']
    },
    children: [
      { path: 'solicitud', component: _import('solicitud/solicitud'), name: 'solicitudlista', meta: { title: 'solicitudlista', roles: ['super', 'admin', 'auxiliar', 'supervisor'] }},
      { path: 'create', component: _import('solicitud/create'), name: 'solicitudcreate', hidden: true, meta: { title: 'solicitudcreate', roles: ['super', 'admin', 'auxiliar', 'supervisor'] }},
      { path: 'edit/:id?', component: _import('solicitud/edit'), name: 'solicitudedit', hidden: true, meta: { title: 'solicitudedit', roles: ['super', 'admin', 'auxiliar', 'almacen', 'supervisor'] }},
      { path: 'almacen', component: _import('solicitud/almacen'), name: 'solicitudalmacen', meta: { title: 'solicitudalmacen', roles: ['super', 'admin', 'auxiliar', 'almacen', 'supervisor'] }},
      { path: 'porvencer', component: _import('solicitud/porvencer'), name: 'solicitudporvencer', hidden: true, meta: { title: 'solicitudporvencer', roles: ['super', 'admin', 'auxiliar', 'supervisor'] }},
      { path: 'ver/:id', component: _import('solicitud/ver'), name: 'solicitudver', hidden: true, meta: { title: 'solicitudver', roles: ['super', 'admin', 'auxiliar', 'supervisor'] }},
      { path: 'informexls', component: _import('solicitud/informexls'), name: 'solicitudinformexls', meta: { title: 'solicitudinformexls', roles: ['super', 'admin', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'informexvencerxls', component: _import('solicitud/informexvencerxls'), name: 'solicitudinformexvencerxls', meta: { title: 'solicitudinformexvencerxls', roles: ['super', 'admin', 'auxiliar', 'supervisor', 'reporte'] }}
    ]
  },
  {
    path: '/informe',
    component: Layout,
    redirect: 'noredirect',
    name: 'informe',
    single: false,
    meta: {
      title: 'informe',
      icon: 'excel',
      roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia', 'supervisor', 'almacenista', 'reporte']
    },
    children: [
      { path: 'rmaterial', component: _import('informe/rmaterial'), name: 'rmaterial', meta: { title: 'rmaterial', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rmaterepo', component: _import('informe/rmaterepo'), name: 'rmaterepo', meta: { title: 'rmaterepo', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rmaterepodet', component: _import('informe/rmaterepodet'), name: 'rmaterepodet', meta: { title: 'rmaterepodet', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rdetalladoexpansion', component: _import('informe/rdetalladoexpansion'), name: 'rdetalladoexpansion', meta: { title: 'rdetalladoexpansion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rdetalladoreubicacion', component: _import('informe/rdetalladoreubicacion'), name: 'rdetalladoreubicacion', meta: { title: 'rdetalladoreubicacion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rdetalladomodernizacion', component: _import('informe/rdetalladomodernizacion'), name: 'rdetalladomodernizacion', meta: { title: 'rdetalladomodernizacion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rdetalladoactualizacion', component: _import('informe/rdetalladoactualizacion'), name: 'rdetalladoactualizacion', meta: { title: 'rdetalladoactualizacion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rdetalladoreposicion', component: _import('informe/rdetalladoreposicion'), name: 'rdetalladoreposicion', meta: { title: 'rdetalladoreposicion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rdetalladorepotenciacion', component: _import('informe/rdetalladorepotenciacion'), name: 'rdetalladorepotenciacion', meta: { title: 'rdetalladorepotenciacion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rdetalladoretiro', component: _import('informe/rdetalladoretiro'), name: 'rdetalladoretiro', meta: { title: 'rdetalladoretiro', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rdetalladoretiroreubicacion', component: _import('informe/rdetalladoretiroreubicacion'), name: 'rdetalladoretiroreubicacion', meta: { title: 'rdetalladoretiroreubicacion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rdetalladocambiomedida', component: _import('informe/rdetalladocambiomedida'), name: 'rdetalladocambiomedida', meta: { title: 'rdetalladocambiomedida', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rmaterialrepetido', component: _import('informe/rmaterialrepetido'), hidden: false, name: 'rmaterialrepetido', meta: { title: 'rmaterialrepetido', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rvisitaporbarrio', component: _import('informe/rvisitaporbarrio'), name: 'rvisitaporbarrio', meta: { title: 'rvisitaporbarrio', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rinventario', component: _import('informe/rinventario'), name: 'rinventario', meta: { title: 'rinventario', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia', 'supervisor', 'reporte'] }},
      { path: 'rdisponibilidad', component: _import('informe/rdisponibilidad'), name: 'rdisponibilidad', meta: { title: 'rdisponibilidad', roles: ['super', 'admin', 'ingeniero', 'almacenista', 'auxiliar', 'gerencia', 'supervisor', 'reporte'] }},
      { path: 'rcalculocarga', component: _import('informe/rcalculocarga'), name: 'rcalculocarga', meta: { title: 'rcalculocarga', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia', 'supervisor', 'reporte'] }},
      { path: 'rconsolidadoreportes', component: _import('informe/rconsolidadoreportes'), name: 'rconsolidadoreportes', meta: { title: 'rconsolidadoreportes', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'reficiencia', component: _import('informe/reficiencia'), name: 'reficiencia', meta: { title: 'reficiencia', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'supervisor', 'reporte'] }},
      { path: 'rgraficos', component: _import('informe/rgraficos'), name: 'rgraficos', meta: { title: 'rgraficos', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia', 'supervisor', 'reporte'] }},
      { path: 'rmedidores', component: _import('informe/rmedidores'), name: 'rmedidores', meta: { title: 'rmedidores', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia', 'supervisor', 'reporte'] }},
      { path: 'rtransformadores', component: _import('informe/rtransformadores'), name: 'rtransformadores', meta: { title: 'rtransformadores', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia', 'supervisor', 'reporte'] }},
      { path: 'rucap', component: _import('informe/rucap'), name: 'rucap', meta: { title: 'rucap', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor', 'reporte'] }},
      { path: 'rmuot', component: _import('informe/rmuot'), name: 'rmuot', meta: { title: 'rmuot', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor', 'auxiliar', 'reporte'] }}
    ]
  },
  {
    path: '/inventario',
    component: Layout,
    redirect: 'noredirect',
    name: 'inventario',
    single: false,
    meta: {
      title: 'inventario',
      icon: 'form',
      roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia']
    },
    children: [
      { path: 'gestion', component: _import('inventario/gestion'), name: 'gestion', meta: { title: 'gestion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia'] }},
      { path: 'eliminada', component: _import('inventario/eliminado'), name: 'eliminada', meta: { title: 'eliminada', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia'] }},
      { path: 'gestion/editar/:id', component: _import('inventario/gestion/edit'), name: 'gestionedit', hidden: true, redirect: false, meta: { title: 'gestionedit', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'gestion/crear', component: _import('inventario/gestion/create'), name: 'gestioncreate', hidden: true, meta: { title: 'gestioncreate', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'consultaporcodigo', component: _import('inventario/consultaporcodigo'), name: 'consultaporcodigo', meta: { title: 'consultaporcodigo', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia'] }},
      { path: 'historia', component: _import('inventario/historia'), name: 'historia', meta: { title: 'historia', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia'] }},
      { path: 'geoposicion', component: _import('inventario/geoposicion'), name: 'geoposicion', meta: { title: 'geoposicionamiento', roles: ['super', 'admin', 'ingeniero', 'gerencia'] }}
    ]
  },
  {
    path: '/control',
    component: Layout,
    redirect: 'noredirect',
    name: 'control',
    single: false,
    meta: {
      title: 'control',
      icon: 'form',
      roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia']
    },
    children: [
      { path: 'gestion', component: _import('control/gestion'), name: 'controlgestion', meta: { title: 'controlgestion', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia'] }},
      { path: 'eliminada', component: _import('control/eliminado'), name: 'controleliminada', meta: { title: 'controleliminada', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia'] }},
      { path: 'gestion/editar/:id', component: _import('control/gestion/edit'), name: 'controlgestionedit', hidden: true, redirect: false, meta: { title: 'controlgestionedit', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'gestion/crear', component: _import('control/gestion/create'), name: 'controlgestioncreate', hidden: true, meta: { title: 'controlgestioncreate', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'consultaporcodigo', component: _import('control/consultaporcodigo'), name: 'controlconsultaporcodigo', meta: { title: 'controlconsultaporcodigo', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia'] }},
      { path: 'historia', component: _import('control/historia'), name: 'controlhistoria', meta: { title: 'controlhistoria', roles: ['super', 'admin', 'ingeniero', 'auxiliar', 'gerencia'] }},
      { path: 'geoposicion', component: _import('control/geoposicion'), name: 'controlgeoposicion', meta: { title: 'controlgeoposicionamiento', roles: ['super', 'admin', 'ingeniero', 'gerencia'] }}
    ]
  },
  {
    path: '/autorizacion',
    component: Layout,
    redirect: 'noredirect',
    name: 'autorizacion',
    single: false,
    meta: {
      title: 'autorizacion',
      icon: 'lock',
      roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor']
    },
    children: [
      { path: 'crearaap', component: _import('autorizacion/crearaap/index'), name: 'crearaap', meta: { title: 'crearaap', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor'] }},
      { path: 'recuperaraap', component: _import('autorizacion/recuperaraap/index'), name: 'recuperaraap', meta: { title: 'recuperaraap', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor'] }},
      { path: 'abrirreporte', component: _import('autorizacion/abrirreporte/index'), name: 'abrirreporte', meta: { title: 'abrirreporte', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor'] }},
      { path: 'cargarreporte', component: _import('autorizacion/cargarreporte/index'), name: 'cargarreporte', meta: { title: 'cargarreporte', roles: ['super', 'admin', 'ingeniero', 'gerencia', 'supervisor'] }}
    ]
  },
  {
    path: '/gerencia',
    component: Layout,
    redirect: 'noredirect',
    name: 'gerencia',
    single: false,
    meta: {
      title: 'gerencia',
      icon: 'star',
      roles: ['super', 'admin', 'ingeniero', 'gerencia']
    },
    children: [
      { path: 'grafica', component: _import('gerencia/grafica'), name: 'grafica', meta: { title: 'grafica', roles: ['super', 'admin', 'ingeniero', 'gerencia'] }},
      { path: 'reporteporuso', component: _import('gerencia/reporteporuso'), name: 'reporteporuso', meta: { title: 'reporteporuso', roles: ['super', 'admin', 'ingeniero', 'gerencia'] }}
    ]
  },
  {
    path: '/administracion',
    component: Layout,
    redirect: 'noredirect',
    name: 'administracion',
    single: false,
    meta: {
      title: 'administracion',
      icon: 'people',
      roles: ['super', 'admin', 'ingeniero', 'auxiliar']
    },
    children: [
      { path: 'ucap', component: _import('administracion/ucap'), name: 'ucap', meta: { title: 'ucap', roles: ['super'] }},
      { path: 'ucap/editar/:id', component: _import('administracion/ucap/edit'), name: 'ucapedit', hidden: true, redirect: false, meta: { title: 'ucapedit', roles: ['super'] }},
      { path: 'ucap/crear', component: _import('administracion/ucap/create'), name: 'ucapcreate', hidden: true, meta: { title: 'ucapcreate', roles: ['super'] }},
      { path: 'unidad', component: _import('administracion/unidad'), name: 'unidad', meta: { title: 'unidad', roles: ['super'] }},
      { path: 'unidad/editar/:id', component: _import('administracion/unidad/edit'), name: 'unidadedit', hidden: true, redirect: false, meta: { title: 'unidadedit', roles: ['super'] }},
      { path: 'unidad/crear', component: _import('administracion/unidad/create'), name: 'unidadcreate', hidden: true, meta: { title: 'unidadcreate', roles: ['super'] }},
      { path: 'caracteristica', component: _import('administracion/caracteristica'), name: 'caracteristica', meta: { title: 'caracteristica', roles: ['super'] }},
      { path: 'caracteristica/editar/:id', component: _import('administracion/caracteristica/edit'), name: 'caracteristicaedit', hidden: true, redirect: false, meta: { title: 'caracteristicaedit', roles: ['super'] }},
      { path: 'caracteristica/crear', component: _import('administracion/caracteristica/create'), name: 'caracteristicacreate', hidden: true, meta: { title: 'caracteristicacreate', roles: ['super'] }},
      { path: 'tipoelemento', component: _import('administracion/tipoelemento'), name: 'tipoelemento', meta: { title: 'tipoelemento', roles: ['super'] }},
      { path: 'tipoelemento/editar/:id', component: _import('administracion/tipoelemento/edit'), name: 'tipoelementoedit', hidden: true, redirect: false, meta: { title: 'tipoelementoedit', roles: ['super'] }},
      { path: 'tipoelemento/crear', component: _import('administracion/tipoelemento/create'), name: 'tipoelementocreate', hidden: true, meta: { title: 'tipoelementocreate', roles: ['super'] }},
      { path: 'elemento', component: _import('administracion/elemento'), name: 'elemento', meta: { title: 'elemento', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'elemento/editar/:id', component: _import('administracion/elemento/edit'), name: 'elementoedit', hidden: true, redirect: false, meta: { title: 'elementoedit', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'elemento/crear', component: _import('administracion/elemento/create'), name: 'elementocreate', hidden: true, meta: { title: 'elementocreate', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'cuadrilla', component: _import('administracion/cuadrilla'), name: 'cuadrilla', meta: { title: 'cuadrilla', roles: ['super', 'admin', 'ingeniero'] }},
      { path: 'cuadrilla/editar/:id', component: _import('administracion/cuadrilla/edit'), name: 'cuadrillaedit', hidden: true, redirect: false, meta: { title: 'cuadrillaedit', roles: ['super', 'admin', 'ingeniero'] }},
      { path: 'cuadrilla/crear', component: _import('administracion/cuadrilla/create'), name: 'cuadrillacreate', hidden: true, meta: { title: 'cuadrillacreate', roles: ['super', 'admin', 'ingeniero'] }},
      { path: 'usuario', component: _import('administracion/usuario'), name: 'usuario', meta: { title: 'usuario', roles: ['super', 'admin', 'ingeniero'] }},
      { path: 'usuario/crear', component: _import('administracion/usuario/create'), name: 'usuariocreate', hidden: true, meta: { title: 'usuariocreate', roles: ['super', 'admin', 'ingeniero'] }},
      { path: 'usuario/editar/:id', component: _import('administracion/usuario/edit'), name: 'usuarioedit', hidden: true, meta: { title: 'usuarioedit', roles: ['super', 'admin', 'ingeniero'] }},
      { path: 'empresa', component: _import('administracion/empresa'), name: 'empresa', meta: { title: 'empresa', roles: ['super'] }},
      { path: 'empresa/editar/:id', component: _import('administracion/empresa/edit'), name: 'empresaedit', hidden: true, meta: { title: 'empresaedit', roles: ['super'] }},
      { path: 'empresa/crear', component: _import('administracion/empresa/create'), name: 'empresacreate', hidden: true, meta: { title: 'empresacreate', roles: ['super'] }},
      { path: 'barrio/lista/:did?/:mid?', component: _import('administracion/barrio'), name: 'barrio', meta: { title: 'barrio', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'barrio/editar/:id', component: _import('administracion/barrio/edit'), name: 'barrioedit', hidden: true, redirect: false, meta: { title: 'barrioedit', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'barrio/crear/:did/:mid', component: _import('administracion/barrio/create'), name: 'barriocreate', hidden: true, meta: { title: 'barriocreate', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'medidor', component: _import('administracion/medidor'), name: 'medidor', meta: { title: 'medidor', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'medidor/editar/:id', component: _import('administracion/medidor/edit'), name: 'medidoredit', hidden: true, redirect: false, meta: { title: 'medidoredit', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'medidor/crear', component: _import('administracion/medidor/create'), name: 'medidorcreate', hidden: true, meta: { title: 'medidorcreate', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'transformador', component: _import('administracion/transformador'), name: 'transformador', meta: { title: 'transformador', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'transformador/editar/:id', component: _import('administracion/transformador/edit'), name: 'transformadoredit', hidden: true, redirect: false, meta: { title: 'transformadoredit', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'transformador/crear', component: _import('administracion/transformador/create'), name: 'transformadorcreate', hidden: true, meta: { title: 'transformadorcreate', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'urbanizadora', component: _import('administracion/urbanizadora'), name: 'urba', meta: { title: 'urba', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'urbanizadora/editar/:id', component: _import('administracion/urbanizadora/edit'), name: 'urbaedit', hidden: true, redirect: false, meta: { title: 'urbaedit', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }},
      { path: 'urbanizadora/crear', component: _import('administracion/urbanizadora/create'), name: 'urbacreate', hidden: true, meta: { title: 'urbacreate', roles: ['super', 'admin', 'ingeniero', 'auxiliar'] }}
    ]
  },
  { path: '*', redirect: '/404', hidden: true }
]
