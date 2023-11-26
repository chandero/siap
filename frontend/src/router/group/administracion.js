import Vue from 'vue'
import Router from 'vue-router'

/* Layout */
import Layout from '../../views/layout/Layout'
// in development-env not use lazy-loading, because lazy-loading too many pages will cause webpack hot update too slow. so only in production use lazy-loading;
// detail: https://panjiachen.github.io/vue-element-admin-site/#/lazy-loading

import admininventarioRole from '../roles/admininventario'
import interventoriaRole from '../roles/interventoria'

export const administradorGroup = {
  path: '/administrador',
  component: Layout,
  redirect: 'noredirect',
  name: 'grupo_administrador',
  single: false,
  meta: {
    title: 'group.administrador',
    icon: 'audit',
    roles: ['super', 'admin', 'administradorsap', 'operadorsap']
  },
  children: [
    admininventarioRole,
    interventoriaRole
  ]
}

export default administradorGroup
