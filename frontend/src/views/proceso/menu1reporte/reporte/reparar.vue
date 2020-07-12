<template>
    <el-container>
        <el-header>
            <span>{{ $t('reporte.reparar') }}</span>
        </el-header>
        <el-main>
          <el-button type="primary" @click="reparar()">{{ $t('reporte.reparar') }}</el-button>
        </el-main>
    </el-container>
</template>
<script>
import { mapGetters } from 'vuex'
import { actualizarHistoria } from '@/api/reporte'
export default {
  data () {
    return {
      labelPosition: 'top'
    }
  },
  computed: {
    ...mapGetters([
      'empresa',
      'usuario'
    ])
  },
  methods: {
    reparar () {
      const loading = this.$loading({
        lock: true,
        text: 'Procesando...',
        // spinner: 'el-icon-loading',
        background: 'rgba(255, 255, 255, 0.7)'
      })
      actualizarHistoria().then(response => {
        loading.close()
        this.$message({
          message: 'Reparación Finalizada',
          type: 'success'
        })
      }).catch(err => {
        loading.close()
        this.$message({
          message: 'Proceso finalizado con error: ' + err,
          type: 'error'
        })
      })
    }
  }
}
</script>
