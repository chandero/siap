import Vue from 'vue'
import Router from 'vue-router'

/* Layout */
import Layout from '../../views/layout/Layout'
// in development-env not use lazy-loading, because lazy-loading too many pages will cause webpack hot update too slow. so only in production use lazy-loading;
// detail: https://panjiachen.github.io/vue-element-admin-site/#/lazy-loading

import pqrsRole from '../roles/pqrs'
import facturacionRole from '../roles/facturacion'
import inventarioRole from '../roles/inventario'
import auditoriaRole from '../roles/auditoria'
import gerenciaRole from '../roles/gerencia'
import supervisorRole from '../roles/supervisor'

export const operadorGroup = {
  path: '/goperador',
  component: Layout,
  redirect: 'noredirect',
  name: 'grupo_operador',
  single: false,
  meta: {
    title: 'group.operador',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap', 'fact', 'facturacion', 'pqrs']
  },
  children: [
    pqrsRole,
    supervisorRole,
    inventarioRole,
    facturacionRole,
    gerenciaRole,
    auditoriaRole
  ]
}

export default operadorGroup
