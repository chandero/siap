<template>
	<div id="app">
    <keep-alive>  
	  	<router-view></router-view>
    </keep-alive>
	</div>
</template>

<script>
  export default{
    name: 'App',
    mounted() {
      this.$store.dispatch('setBaseUrl', {
        url: window.location.protocol + '//' + window.location.host.split('/')[0].split(':')[0] + ':9091/api'
      })
    }
  }
</script>
