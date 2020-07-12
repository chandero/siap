<template>
  <el-container>
    <el-header>
        {{ aap_id }}
    </el-header>
  </el-container>
</template>
<script>
export default {
  name: 'historial',
  props: {
    aap_id: {
      type: Number,
      require: true
    }
  }
}
</script>
