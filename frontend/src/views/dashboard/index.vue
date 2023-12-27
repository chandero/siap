<template>
  <div class="dashboard-container">
    <component :is="currentRole"></component>
  </div>
</template>

<script>
import { mapGetters } from 'vuex'
import adminDashboard from './admin'
import operativoDashboard from './operativo'
import ingenieroDashboard from './ingeniero'

export default {
  name: 'dashboard',
  components: { adminDashboard, operativoDashboard, ingenieroDashboard },
  data () {
    return {
      currentRole: 'adminDashboard'
    }
  },
  computed: {
    ...mapGetters([
      'roles'
    ])
  },
  created () {
    console.log('roles', this.roles)
    if (this.roles.includes('pqrs')) {
      this.currentRole = 'adminDashboard'
    } else {
      this.currentRole = 'adminDashboard'
    } //   this.currentRole = 'operativoDashboard'
    // }
  }
}
</script>
