<template>
  <el-container>
    <el-dialog
      title="SIAP-Sesión"
      :visible.sync="dialogVisible"
      width="50%"
      close-on-click-modal="false"
      close-on-press-escape="false"
      show-close="false">
      <span>Ud ha dejado este aplicativo inactivo por más de 10 minutos. Se cerrara la sesión.</span>
      <span slot="footer" class="dialog-footer">
      <el-button type="primary" @click="cancelar">Cancelar</el-button>
  </span>      
    </el-dialog>
  </el-container>
</template>
<script>
export default {
  data() {
    return {
      time: 10000,
      dialogVisible: false,
      reset: false
    }
  },
  created() {
    const timerId = setInterval(() => {
      this.time -= 1000
      if (!this.$store.state.idleVue.isIdle) clearInterval(timerId)
      if (this.reset) clearInterval(timerId)
      if (this.time < 1) {
        clearInterval(timerId)
        this.logout()
      }
    }, 1000)
  },
  methods: {
    cancelar() {
      this.reset = true
    },
    logout() {
      this.dialogVisible = false
      this.$store.dispatch('LogOut').then(() => {
        location.reload()// In order to re-instantiate the vue-router object to avoid bugs
      })
    }
  },
  beforeMount() {
    this.dialogVisible = true
  }
}
</script>