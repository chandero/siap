<template>
<el-container v-loading="loading"
		:element-loading-text="$t('lostserverconnection')"
    element-loading-spinner="el-icon-loading"
    element-loading-background="rgba(0, 0, 0, 0.8)"
		>
		<el-main>
      <el-row>
        <el-col :xs="24" :sm="24" :md="3" :lg="3" :xl="3">
          <logo class="sidebar-logo-container"></logo>
        </el-col>
        <el-col :xs="24" :sm="24" :md="21" :lg="21" :xl="21">
			    <navbar></navbar>
        </el-col>
      </el-row>
      <el-row>
        <menubar></menubar>
      </el-row>
      <el-row>
			  <tags-view><span>Is Idle - {{ isIdle }}</span></tags-view>
      </el-row>
      <el-row>
			<app-main></app-main>
      </el-row>
		</el-main>
  <ModalIdle v-if="isIdle"/>
</el-container>
</template>
<script>
import { isReachable } from '@/api/isreachable'
import { Navbar, Menubar, AppMain, TagsView, Logo } from './components'
import ModalIdle from '@/components/ModalIdle'

export default {
  name: 'layout',
  components: {
    ModalIdle,
    Navbar,
    Menubar,
    AppMain,
    TagsView,
    Logo
  },
  timers: {
    checkServer: { name: 'checkServer', time: 5000, autostart: true, repeat: true }
  },
  data () {
    return {
      loading: false
    }
  },
  computed: {
    sidebar () {
      return this.$store.state.app.sidebar
    },
    menubar () {
      return this.$store.state.app.menubar
    },
    isIdle () {
      return this.$store.state.idleVue.isIdle
    }
  },
  methods: {
    checkServer () {
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
    display: inline-flexbox;
	  position: relative;
	  height: 100%;
	  width: 100%;
  }
  .app_main {
    max-height: 800px;
  }
  .menubar-container {
    max-height: 60px;
  }
</style>
