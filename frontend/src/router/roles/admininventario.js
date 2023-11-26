import Vue from 'vue'
import Router from 'vue-router'

/* Layout */
import Role from '../../views/grupo/role'

// in development-env not use lazy-loading, because lazy-loading too many pages will cause webpack hot update too slow. so only in production use lazy-loading;
// detail: https://panjiachen.github.io/vue-element-admin-site/#/lazy-loading

import informeRouter from '../modules/informe'

export const admininventarioRole = {
  path: '/admininventario',
  component: Role,
  redirect: 'noredirect',
  name: 'admininventario',
  single: false,
  meta: {
    title: 'role.inventario',
    icon: 'inventory',
    roles: ['super', 'admin', 'admininventario']
  },
  children: [
    informeRouter
  ]
}

export default admininventarioRole
