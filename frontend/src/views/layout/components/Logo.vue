<template>
  <div>
    <transition name="sidebarLogoFade">
      <router-link v-if="collapse" key="collapse" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="minilogo" class="sidebar-logo">
        <h1 v-else class="sidebar-title">{{ title }} </h1>
      </router-link>
      <router-link v-else key="expand" class="sidebar-logo-link" to="/">
        <img v-if="logo" :src="logo" class="sidebar-logo">
        <h1 class="sidebar-title">{{ title }} </h1>
      </router-link>
    </transition>
  </div>
</template>

<script>
import settings from '@/settings'

export default {
  data () {
    return {
      collapse: false,
      title: settings.title,
      logo: 'logoisag.png',
      minilogo: 'farolisag.png'
    }
  }
}
</script>

<style lang="scss" scoped>
.sidebarLogoFade-enter-active {
  transition: opacity 1.5s;
}
.sidebarLogoFade-enter,
.sidebarLogoFade-leave-to {
  opacity: 0;
}
.sidebar-logo-container {
  position: relative;
  width: 160px;
  height: 50px;
  line-height: 50px;
  // background: #2b2f3a;
  text-align: center;
  overflow: hidden;
  & .sidebar-logo-link {
    height: 50px !important;
    width: 50px;
    & .sidebar-logo {
      // width: 32px;
      height: 32px;
      vertical-align: middle;
      margin-right: 12px;
    }
    & .sidebar-title {
      display: inline-block;
      margin: 0;
      color: #2b2f3a;
      // color: #fff;
      font-weight: 600;
      line-height: 50px;
      font-size: 14px;
      font-family: Avenir, Helvetica Neue, Arial, Helvetica, sans-serif;
      vertical-align: middle;
    }
  }
  &.collapse {
    .sidebar-logo {
      margin-right: 0px;
    }
  }
}
</style>
