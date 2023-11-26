import Vue from 'vue'
import Router from 'vue-router'

/* Layout */
import Role from '../../views/grupo/role'
// in development-env not use lazy-loading, because lazy-loading too many pages will cause webpack hot update too slow. so only in production use lazy-loading;
// detail: https://panjiachen.github.io/vue-element-admin-site/#/lazy-loading

import inventarioRouter from '../modules/inventario'
import procesoRouter from '../modules/proceso'
import actaObraRouter from '../modules/acta-obra'
import informeRouter from '../modules/informe'
import solicitudRouter from '../modules/solicitud'
import auditorRouter from '../modules/auditor'

export const auditoriaRole = {
  path: '/auditoria',
  component: Role,
  redirect: 'noredirect',
  name: 'auditoria',
  single: false,
  meta: {
    title: 'role.auditoria',
    icon: 'audit',
    roles: ['super', 'admin', 'auditoria']
  },
  children: [
    procesoRouter,
    informeRouter,
    inventarioRouter
  ]
}

export default auditoriaRole
