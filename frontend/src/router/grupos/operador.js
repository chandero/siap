import Vue from 'vue'
import Router from 'vue-router'

/* Layout */
import Layout from '../../views/layout/Layout'
import perfilAuditoria from '@/router/Operadordelegado/perfilAuditoria'
import perfilFacturacion from '@/router/Operadordelegado/perfilFacturacion'
import perfilGerencia from '@/router/Operadordelegado/perfilGerencia'
import perfilInventario from '@/router/Operadordelegado/perfilInventario'
import perfilPqrs from '@/router/Operadordelegado/perfilPqrs'
import perfilSupervisor from '@/router/Operadordelegado/perfilSupervisor'
const grupoOperador = {
    path: '/operador',
    component: Layout,
    redirect: 'noredirect',
    name: 'operadorGrupo',
    single: false,
    meta: {
    title: 'Operador Grupo',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap', 'fact', 'facturacion', 'pqrs']
  },
  children: [
    perfilAuditoria,
    perfilFacturacion,
    perfilGerencia,
    perfilInventario,
    perfilPqrs,
    perfilSupervisor
  ]
}
export default grupoOperador