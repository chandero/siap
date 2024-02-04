<template>
  <div>
      <el-menu :default-active="$route.path" :background-color="variables.menuBg"
          :text-color="variables.menuText" :active-text-color="variables.menuActiveText" mode="horizontal" class="top-menu-bar">
          <topbar-item v-for="route in permission_routers" :key="route.path" :item="route" :base-path="route.path" />
      </el-menu>
  </div>
</template>
<script>
import { mapGetters } from 'vuex'
import TopbarItem from './TopbarItem'
import Logo from './Logo'
import variables from '@/styles/variables.scss'

export default {
  components: { TopbarItem, Logo },
  computed: {
    ...mapGetters([
      'permission_routers',
      'sidebar'
    ]),
    showLogo() {
      return this.$store.state.settings.sidebarLogo
    },
    variables() {
      return variables
    },
    isCollapse() {
      return !this.sidebar.opened
    }
  }
}
</script>
<style lang="scss" scoped>
.top-menu-bar {
  background-color: #fff;
  border-bottom: 1px solid #ebeef5;
  box-shadow: 0 2px 12px 0 rgba(0, 0, 0, 0.1);
  position: relative;
  text-align: left;
  align-self: left;
}
</style>
