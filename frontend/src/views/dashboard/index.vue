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
    switch (this.roles) {
      case 'super':
        this.currentRole = 'adminDashboard'
        break
      case 'admin':
        this.currentRole = 'adminDashboard'
        break
      case 'operativo':
        this.currentRole = 'operativoDashboard'
        break
      case 'ingeniero':
        this.currentRole = 'ingenieroDashboard'
        break
      default:
        this.currentRole = 'operativoDashboard'
        break
    }
    // if (!this.roles.includes('admin')) {
    //   this.currentRole = 'operativoDashboard'
    // }
  }
}
</script>
