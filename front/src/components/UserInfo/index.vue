<template>
    <div class="userinfo">
        <svg-icon icon-class="user"></svg-icon>
        <span>{{ name }} - {{ empresa.empr_descripcion }} </span>
    </div>
</template>
<script>
import { mapGetters } from 'vuex'

export default {
  name: 'userinfo',
  computed: {
    ...mapGetters([
      'name',
      'empresa'
    ])
  }
}
</script>

<style rel="stylesheet/scss" lang="scss" scoped>
  .userinfo {
    display: table-cell;
    text-transform: uppercase;
    font-size: 14px;
    line-height: 25px;
    margin-left: 10px;
  }
</style>
