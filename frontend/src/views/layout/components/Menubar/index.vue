<template>
  <el-menu :default-active="$route.path" :background-color="variables.menuBg" :text-color="variables.menuText"
    :active-text-color="variables.menuActiveText" mode="horizontal">
    <menubar-item v-for="route in permission_routers" :key="route.path" :item="route" :base-path="route.path" />
  </el-menu>
</template>

<script>
import { mapGetters } from 'vuex'
import MenubarItem from './MenubarItem'
import Logo from './Logo'
import variables from '@/styles/variables.scss'

export default {
  components: { MenubarItem, Logo },
  computed: {
    ...mapGetters([
      'permission_routers',
      'menubar'
    ]),
    showLogo() {
      return false
    },
    variables() {
      return variables
    },
    isCollapse() {
      return false
    }
  }
}
</script>
