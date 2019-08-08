<template>
<el-container v-loading="loading"    
		:element-loading-text="$t('lostserverconnection')"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.8)"
		>
	<div class="app-wrapper" :class="{hideSidebar:!sidebar.opened}">
		<sidebar class="sidebar-container"></sidebar>
		<div class="main-container">
			<navbar></navbar>
			<tags-view></tags-view>
			<app-main></app-main>
		</div>
	</div>
</el-container>
</template>

<script>
import { isReachable } from '@/api/isreachable'
import { Navbar, Sidebar, AppMain, TagsView } from './components'

export default {
  name: 'layout',
  components: {
    Navbar,
    Sidebar,
    AppMain,
    TagsView
  },
  timers: {
    checkServer: { name: 'checkServer', time: 5000, autostart: true, repeat: true }
  },
  data() {
    return {
      loading: false
    }
  },
  computed: {
    sidebar() {
      return this.$store.state.app.sidebar
    }
  },
  methods: {
    checkServer() {
      isReachable().then(reachable => {
        if (reachable) {
          this.loading = false
        } else {
          this.loading = true
        }
      }).catch(error => {
        this.loading = true
        console.log(error)
      })
    }
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
	@import "src/styles/mixin.scss";
	.app-wrapper {
	  @include clearfix;
	  position: relative;
	  height: 100%;
	  width: 100%;
  }
  .app_main {
    max-height: 800px;
  }
</style>
