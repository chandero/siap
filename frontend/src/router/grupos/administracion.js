import Vue from 'vue'
import Router from 'vue-router'

/* Layout */
import Layout from '../../views/layout/Layout'
import perfilInterventoria from "@/router/Administracion/perfilInterventoria"
import perfilInventario from "@/router/Administracion/perfilInventario"
const grupoAdministracion =  {
    path: '/operador',
    component: Layout,
    redirect: 'noredirect',
    name: 'operadorGrupo',
    single: false,
    meta: {
    title: 'Operador Grupo',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap']
  },
  children: [
    perfilInterventoria,
    perfilInventario
  ]
}
export default grupoAdministracion