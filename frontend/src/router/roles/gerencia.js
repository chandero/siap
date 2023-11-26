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
import supervisorRouter from '../modules/supervisor'
import gerenciaRouter from '../modules/gerencia'
import administracionRouter from '../modules/administracion'

export const gerenciaRole = {
  path: '/gerencia',
  component: Role,
  redirect: 'noredirect',
  name: 'gerencia',
  single: false,
  meta: {
    title: 'role.gerencia',
    icon: 'ceo',
    roles: ['super', 'admin', 'gerencia']
  },
  children: [
    gerenciaRouter,
    informeRouter,
    inventarioRouter
  ]
}

export default gerenciaRole
