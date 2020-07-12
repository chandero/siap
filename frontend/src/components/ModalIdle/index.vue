<template>
  <el-container>
    <el-dialog
      title="SIAP-Sesión"
      :visible.sync="dialogVisible"
      width="50%"
      :close-on-click-modal="false"
      :close-on-press-escape="false"
      :show-close="false">
      <span>Después de 10 minutos de inactividad, SIAP finalizará la sesión.</span>
      <span>Tiempo Faltante {{ hora(time/1000) }} </span>
      <span slot="footer" class="dialog-footer">
  </span>
    </el-dialog>
  </el-container>
</template>
<script>
export default {
  data () {
    return {
      time: 600000,
      dialogVisible: false
    }
  },
  created () {
    const timerId = setInterval(() => {
      this.time -= 1000
      if (!this.$store.state.idleVue.isIdle) clearInterval(timerId)
      if (this.time < 1) {
        clearInterval(timerId)
        this.logout()
      }
    }, 1000)
  },
  methods: {
    hora (time) {
      var minutes = Math.floor(time / 60)
      var seconds = time % 60
      // Anteponiendo un 0 a los minutos si son menos de 10
      minutes = minutes < 10 ? '0' + minutes : minutes
      // Anteponiendo un 0 a los segundos si son menos de 10
      seconds = seconds < 10 ? '0' + seconds : seconds
      return minutes + ':' + seconds
    },
    logout () {
      this.dialogVisible = false
      this.$store.dispatch('LogOut').then(() => {
        location.reload()// In order to re-instantiate the vue-router object to avoid bugs
      })
    }
  },
  beforeMount () {
    this.dialogVisible = true
  }
}
</script>
